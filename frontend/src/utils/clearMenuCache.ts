/**
 * 清除菜单缓存工具
 * 用于清除 localStorage 中的菜单缓存，强制重新从后端获取最新菜单数据
 */
import { storageLocal } from "@pureadmin/utils";

/**
 * 清除菜单缓存
 */
export function clearMenuCache() {
  const key = "async-routes";
  storageLocal().removeItem(key);
  console.log("菜单缓存已清除，下次登录时会重新获取最新菜单数据");
}

/**
 * 检查并清除菜单缓存（如果缓存存在）
 */
export function checkAndClearMenuCache() {
  const key = "async-routes";
  const cached = storageLocal().getItem(key);
  if (cached) {
    storageLocal().removeItem(key);
    console.log("检测到菜单缓存，已自动清除");
    return true;
  }
  return false;
}
