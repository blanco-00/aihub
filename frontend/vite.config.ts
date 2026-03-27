import { getPluginsList } from "./build/plugins";
import { include, exclude } from "./build/optimize";
import { type UserConfigExport, type ConfigEnv, loadEnv } from "vite";
import {
  root,
  alias,
  wrapperEnv,
  pathResolve,
  __APP_INFO__
} from "./build/utils";

export default ({ mode }: ConfigEnv): UserConfigExport => {
  const { VITE_CDN, VITE_PORT, VITE_COMPRESSION, VITE_PUBLIC_PATH } =
    wrapperEnv(loadEnv(mode, root));
  return {
    base: VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias
    },
    // 服务端渲染
    server: {
      // 端口号
      port: VITE_PORT,
      host: "0.0.0.0",
      // 本地跨域代理 https://cn.vitejs.dev/config/server-options.html#server-proxy
      proxy: {
        "/api": {
          // 性能优化：使用 127.0.0.1 而不是 localhost，避免 DNS 解析延迟
          // 注意：如果使用 localhost，浏览器/Node.js 可能需要解析 DNS，可能导致延迟
          target: "http://127.0.0.1:9528",
          changeOrigin: true,
          ws: true,
          // 性能优化：配置代理超时和连接复用
          // 注意：http-proxy-middleware 的默认 proxyTimeout 是 120000ms（2分钟），timeout 是 undefined
          // 这里明确设置，避免使用可能存在的默认5秒超时
          timeout: 30000,  // 代理超时时间（30秒，避免过早超时）
          proxyTimeout: 30000,  // 代理请求超时时间（30秒）
          // 性能优化：配置连接选项
          secure: false,  // 如果是 https，设置为 false 跳过证书验证（仅开发环境）
          // 注意：keepAlive 是 Node.js 的默认行为，但我们可以通过其他方式优化
          // 使用 127.0.0.1 而不是 localhost 可能更快（避免 DNS 解析）
          // 但这里保持 localhost 以便于理解
          // 配置代理选项，优化连接复用和性能追踪
          configure: (proxy, _options) => {
            proxy.on('proxyReq', (proxyReq, req, res) => {
              // 添加连接复用头
              proxyReq.setHeader('Connection', 'keep-alive');
              // 记录代理请求开始时间（用于性能追踪）
              (req as any).__proxyStartTime = Date.now();
            });
            proxy.on('proxyRes', (proxyRes, req, res) => {
              // 只记录性能警告和错误
              const proxyStartTime = (req as any).__proxyStartTime;
              if (proxyStartTime) {
                const proxyTime = Date.now() - proxyStartTime;
                const serverTime = proxyRes.headers['x-response-time'];
                
                if (proxyTime > 1000) {
                  console.error(`[Vite Proxy 严重警告] 代理请求耗时过长: ${proxyTime}ms, URL: ${req.url}, 后端耗时: ${serverTime || 'N/A'}`);
                } else if (proxyTime > 500) {
                  console.warn(`[Vite Proxy 警告] 代理请求耗时: ${proxyTime}ms, URL: ${req.url}`);
                }
              }
            });
            proxy.on('error', (err, req, res) => {
              console.error('[Vite Proxy Error]', {
                message: err.message,
                url: req.url,
                method: req.method
              });
            });
          }
        }
      },
      // 预热文件以提前转换和缓存结果，降低启动期间的初始页面加载时长并防止转换瀑布
      warmup: {
        clientFiles: ["./index.html", "./src/{views,components}/*"]
      }
    },
    plugins: getPluginsList(VITE_CDN, VITE_COMPRESSION),
    // https://cn.vitejs.dev/config/dep-optimization-options.html#dep-optimization-options
    optimizeDeps: {
      include,
      exclude
    },
    build: {
      // https://cn.vitejs.dev/guide/build.html#browser-compatibility
      target: "es2015",
      sourcemap: false,
      // 消除打包大小超过500kb警告
      chunkSizeWarningLimit: 4000,
      rollupOptions: {
        input: {
          index: pathResolve("./index.html", import.meta.url)
        },
        // 静态资源分类打包
        output: {
          chunkFileNames: "static/js/[name]-[hash].js",
          entryFileNames: "static/js/[name]-[hash].js",
          assetFileNames: "static/[ext]/[name]-[hash].[ext]"
        }
      }
    },
    define: {
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__)
    }
  };
};
