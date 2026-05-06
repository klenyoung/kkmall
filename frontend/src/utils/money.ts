export function money(cent: number) {
  return `￥${(Number(cent || 0) / 100).toFixed(2)}`
}
