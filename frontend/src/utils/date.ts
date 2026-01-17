import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";

// 扩展 dayjs 插件
dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

/**
 * 时间格式化工具函数
 */

/**
 * 格式化日期时间（标准格式：YYYY-MM-DD HH:mm:ss）
 * @param date 日期时间（可以是 Date、string、number 或 dayjs 对象）
 * @param format 格式化模板，默认为 "YYYY-MM-DD HH:mm:ss"
 * @returns 格式化后的日期时间字符串，如果日期无效则返回 "-"
 */
export function formatDateTime(
  date: string | number | Date | null | undefined,
  format: string = "YYYY-MM-DD HH:mm:ss"
): string {
  if (!date) {
    return "-";
  }
  const d = dayjs(date);
  if (!d.isValid()) {
    return "-";
  }
  return d.format(format);
}

/**
 * 格式化日期（标准格式：YYYY-MM-DD）
 * @param date 日期（可以是 Date、string、number 或 dayjs 对象）
 * @param format 格式化模板，默认为 "YYYY-MM-DD"
 * @returns 格式化后的日期字符串，如果日期无效则返回 "-"
 */
export function formatDate(
  date: string | number | Date | null | undefined,
  format: string = "YYYY-MM-DD"
): string {
  if (!date) {
    return "-";
  }
  const d = dayjs(date);
  if (!d.isValid()) {
    return "-";
  }
  return d.format(format);
}

/**
 * 格式化时间（标准格式：HH:mm:ss）
 * @param date 时间（可以是 Date、string、number 或 dayjs 对象）
 * @param format 格式化模板，默认为 "HH:mm:ss"
 * @returns 格式化后的时间字符串，如果日期无效则返回 "-"
 */
export function formatTime(
  date: string | number | Date | null | undefined,
  format: string = "HH:mm:ss"
): string {
  if (!date) {
    return "-";
  }
  const d = dayjs(date);
  if (!d.isValid()) {
    return "-";
  }
  return d.format(format);
}

/**
 * 格式化相对时间（例如：2小时前、3天前）
 * @param date 日期时间（可以是 Date、string、number 或 dayjs 对象）
 * @returns 相对时间字符串，如果日期无效则返回 "-"
 */
export function formatRelativeTime(
  date: string | number | Date | null | undefined
): string {
  if (!date) {
    return "-";
  }
  const d = dayjs(date);
  if (!d.isValid()) {
    return "-";
  }
  return d.fromNow();
}

/**
 * 格式化日期时间（简短格式：YYYY-MM-DD HH:mm）
 * @param date 日期时间（可以是 Date、string、number 或 dayjs 对象）
 * @returns 格式化后的日期时间字符串，如果日期无效则返回 "-"
 */
export function formatDateTimeShort(
  date: string | number | Date | null | undefined
): string {
  return formatDateTime(date, "YYYY-MM-DD HH:mm");
}

/**
 * 格式化日期时间（完整格式：YYYY-MM-DD HH:mm:ss.SSS）
 * @param date 日期时间（可以是 Date、string、number 或 dayjs 对象）
 * @returns 格式化后的日期时间字符串，如果日期无效则返回 "-"
 */
export function formatDateTimeFull(
  date: string | number | Date | null | undefined
): string {
  return formatDateTime(date, "YYYY-MM-DD HH:mm:ss.SSS");
}

/**
 * 检查日期是否有效
 * @param date 日期时间（可以是 Date、string、number 或 dayjs 对象）
 * @returns 如果日期有效返回 true，否则返回 false
 */
export function isValidDate(
  date: string | number | Date | null | undefined
): boolean {
  if (!date) {
    return false;
  }
  return dayjs(date).isValid();
}

/**
 * 获取当前日期时间（格式化后的字符串）
 * @param format 格式化模板，默认为 "YYYY-MM-DD HH:mm:ss"
 * @returns 格式化后的当前日期时间字符串
 */
export function getCurrentDateTime(format: string = "YYYY-MM-DD HH:mm:ss"): string {
  return dayjs().format(format);
}

/**
 * 导出 dayjs 实例，方便直接使用
 */
export { dayjs };
