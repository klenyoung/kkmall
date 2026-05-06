import os
import posixpath
import re
import socket
import sys
import time
import json
from pathlib import Path

import paramiko
import pymysql
from minio import Minio
from minio.error import S3Error


ROOT = Path(__file__).resolve().parents[1]
ENV_FILE = ROOT / ".env"
JAR_FILE = ROOT / "backend" / "target" / "kkmall-backend-0.1.0.jar"
SCHEMA_FILE = ROOT / "backend" / "src" / "main" / "resources" / "db" / "schema.sql"
SEED_FILE = ROOT / "backend" / "src" / "main" / "resources" / "db" / "seed.sql"


def load_env():
    data = {}
    for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" in line:
            key, value = line.split("=", 1)
        elif ":" in line:
            key, value = line.split(":", 1)
        else:
            continue
        data[key.strip()] = value.strip().strip('"').strip("'")
    return data


def require(env, *keys):
    for key in keys:
        value = env.get(key)
        if value:
            return value
    raise RuntimeError(f"Missing required env key: {'/'.join(keys)}")


def mysql_statements(path):
    text = path.read_text(encoding="utf-8")
    text = re.sub(r"--.*", "", text)
    statements = []
    current = []
    in_quote = None
    escape = False
    for char in text:
        current.append(char)
        if in_quote:
            if escape:
                escape = False
            elif char == "\\":
                escape = True
            elif char == in_quote:
                in_quote = None
        elif char in ("'", '"', "`"):
            in_quote = char
        elif char == ";":
            statement = "".join(current).strip()
            if statement:
                statements.append(statement[:-1].strip())
            current = []
    tail = "".join(current).strip()
    if tail:
        statements.append(tail)
    return [stmt for stmt in statements if stmt]


def init_mysql(env):
    host = require(env, "linux_sever_ip", "linux_server_ip")
    user = env.get("USER", "root")
    password = require(env, "MYSQL_ROOT_PASSWORD")
    conn = pymysql.connect(host=host, port=3306, user=user, password=password, charset="utf8mb4", autocommit=True)
    try:
        with conn.cursor() as cur:
            for path in (SCHEMA_FILE, SEED_FILE):
                for stmt in mysql_statements(path):
                    cur.execute(stmt)
            repair_seed_data(cur)
        print("mysql: initialized kkmall schema and seed data")
    finally:
        conn.close()


def repair_seed_data(cur):
    cur.execute("UPDATE users SET nickname=%s WHERE id=1", ("\u7ba1\u7406\u5458",))
    categories = [
        (1, "\u670d\u9970"),
        (2, "\u6570\u7801"),
        (3, "\u5bb6\u5c45"),
    ]
    cur.executemany("UPDATE categories SET name=%s WHERE id=%s", [(name, item_id) for item_id, name in categories])
    products = [
        (1, 1, "\u8212\u9002\u7eaf\u68c9\u57fa\u7840\u6b3e T \u6064", "\u67d4\u8f6f\u68c9\u611f\uff0c\u9002\u5408\u65e5\u5e38\u901a\u52e4\u4e0e\u5468\u672b\u51fa\u884c\u3002", ["T"]),
        (2, 1, "\u8f7b\u91cf\u9632\u6cfc\u6c34\u901a\u52e4\u53cc\u80a9\u5305", "\u591a\u9694\u5c42\u6536\u7eb3\uff0c\u8f7b\u91cf\u8010\u78e8\u3002", ["B"]),
        (3, 2, "\u84dd\u7259\u964d\u566a\u8033\u673a Pro", "\u8212\u9002\u4f69\u6234\uff0c\u957f\u7eed\u822a\u3002", ["E"]),
        (4, 2, "\u684c\u9762\u65e0\u7ebf\u5feb\u5145\u652f\u67b6", "\u7acb\u5f0f\u89c2\u770b\uff0c\u968f\u653e\u968f\u5145\u3002", ["C"]),
        (5, 3, "\u5317\u6b27\u98ce\u9676\u74f7\u9a6c\u514b\u676f", "\u6e29\u6da6\u91c9\u9762\uff0c\u5bb9\u91cf\u9002\u4e2d\u3002", ["M"]),
        (6, 3, "\u53ef\u6298\u53e0\u6536\u7eb3\u6574\u7406\u7bb1", "\u7a33\u56fa\u53e0\u653e\uff0c\u900f\u660e\u53ef\u89c6\u3002", ["S"]),
    ]
    for item_id, category_id, title, description, images in products:
        cur.execute(
            "UPDATE products SET category_id=%s, title=%s, description=%s, images=CAST(%s AS JSON), status='ON_SALE' WHERE id=%s",
            (category_id, title, description, json.dumps(images), item_id),
        )
    skus = [
        (101, "\u989c\u8272", "\u767d\u8272", 9900, 20),
        (102, "\u989c\u8272", "\u9ed1\u8272", 9900, 12),
        (201, "\u989c\u8272", "\u66dc\u77f3\u9ed1", 15900, 9),
        (202, "\u989c\u8272", "\u96fe\u7070", 15900, 6),
        (301, "\u989c\u8272", "\u4e91\u767d", 29900, 8),
        (302, "\u989c\u8272", "\u591c\u9ed1", 29900, 5),
        (401, "\u989c\u8272", "\u94f6\u7070", 8900, 14),
        (501, "\u5bb9\u91cf", "350ml", 6900, 25),
        (601, "\u5c3a\u5bf8", "\u4e2d\u53f7", 7900, 18),
    ]
    for sku_id, spec_name, spec_value, price, stock in skus:
        cur.execute(
            "UPDATE skus SET spec_name=%s, spec_value=%s, price=%s, stock=%s WHERE id=%s",
            (spec_name, spec_value, price, stock, sku_id),
        )


def init_minio(env):
    host = require(env, "linux_sever_ip", "linux_server_ip")
    access = require(env, "MINIO_USER")
    secret = require(env, "MINIO_PASS")
    bucket = env.get("MINIO_BUCKET", "kkmall")
    client = Minio(f"{host}:9000", access_key=access, secret_key=secret, secure=False)
    try:
        if not client.bucket_exists(bucket):
            client.make_bucket(bucket)
            print(f"minio: created bucket {bucket}")
        else:
            print(f"minio: bucket {bucket} already exists")
    except S3Error as exc:
        raise RuntimeError(f"MinIO bucket initialization failed: {exc}") from exc


def ssh_connect(env):
    host = require(env, "linux_sever_ip", "linux_server_ip")
    username = require(env, "user")
    password = require(env, "password")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(hostname=host, username=username, password=password, timeout=20)
    return ssh


def run(ssh, command, sudo_password=None, check=True):
    if sudo_password:
        command = f"printf '%s\\n' {shell_quote(sudo_password)} | sudo -S bash -lc {shell_quote(command)}"
    stdin, stdout, stderr = ssh.exec_command(command, get_pty=bool(sudo_password), timeout=120)
    out = stdout.read().decode("utf-8", errors="replace")
    err = stderr.read().decode("utf-8", errors="replace")
    code = stdout.channel.recv_exit_status()
    if check and code != 0:
        raise RuntimeError(f"Command failed ({code}): {command}\nSTDOUT:\n{out}\nSTDERR:\n{err}")
    return code, out, err


def shell_quote(value):
    return "'" + value.replace("'", "'\"'\"'") + "'"


def upload_backend(env):
    if not JAR_FILE.exists():
        raise RuntimeError(f"Jar not found: {JAR_FILE}")
    mysql_password = require(env, "MYSQL_ROOT_PASSWORD")
    auth_secret = env.get("KKMALL_AUTH_SECRET", "kkmall-change-me-" + str(int(time.time())))
    host = require(env, "linux_sever_ip", "linux_server_ip")
    ssh_password = require(env, "password")
    ssh = ssh_connect(env)
    try:
        run(ssh, "mkdir -p /tmp/kkmall-upload")
        sftp = ssh.open_sftp()
        try:
            sftp.put(str(JAR_FILE), "/tmp/kkmall-upload/kkmall-backend-0.1.0.jar")
            service = (ROOT / "deploy" / "kkmall-backend.service").read_text(encoding="utf-8")
            with sftp.file("/tmp/kkmall-upload/kkmall-backend.service", "w") as remote:
                remote.write(service)
        finally:
            sftp.close()
        remote_env = "\n".join([
            "SERVER_PORT=8080",
            f"MYSQL_URL=jdbc:mysql://127.0.0.1:3306/kkmall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai",
            "MYSQL_USER=root",
            f"MYSQL_PASSWORD={mysql_password}",
            f"KKMALL_AUTH_SECRET={auth_secret}",
            "KKMALL_MOCK_CODE=123456",
            "",
        ])
        setup = (
            "mkdir -p /opt/kkmall/backend && "
            "cp /tmp/kkmall-upload/kkmall-backend-0.1.0.jar /opt/kkmall/backend/kkmall-backend-0.1.0.jar && "
            "cp /tmp/kkmall-upload/kkmall-backend.service /etc/systemd/system/kkmall-backend.service && "
            f"cat > /opt/kkmall/backend/.env <<'EOF'\n{remote_env}EOF\n"
            "systemctl daemon-reload && systemctl enable kkmall-backend && systemctl restart kkmall-backend"
        )
        run(ssh, setup, sudo_password=ssh_password)
        time.sleep(5)
        code, out, err = run(ssh, "systemctl is-active kkmall-backend", sudo_password=ssh_password, check=False)
        print(f"backend: systemd status {out.strip() or err.strip()}")
        return host
    finally:
        ssh.close()


def wait_api(host):
    import urllib.request
    url = f"http://{host}:8080/api/v1/categories"
    for _ in range(20):
        try:
            with urllib.request.urlopen(url, timeout=5) as resp:
                body = resp.read().decode("utf-8", errors="replace")
                if resp.status == 200:
                    print("api: categories endpoint reachable")
                    return body
        except Exception:
            time.sleep(2)
    raise RuntimeError("API did not become reachable")


def main():
    env = load_env()
    print("provision: using server", require(env, "linux_sever_ip", "linux_server_ip"))
    init_mysql(env)
    init_minio(env)
    host = upload_backend(env)
    wait_api(host)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)
