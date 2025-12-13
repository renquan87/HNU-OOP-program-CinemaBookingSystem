import { getPluginsList } from "./build/plugins";
import { include, exclude } from "./build/optimize";
import { type ConfigEnv, loadEnv, defineConfig } from "vite";
import {
  root,
  alias,
  wrapperEnv,
  pathResolve,
  __APP_INFO__
} from "./build/utils";

export default defineConfig(({ mode }: ConfigEnv) => {
  // 1. 修复 'env' unused: 直接在这里使用 loadEnv，不需要单独声明 const env
  // 2. 修复 'VITE_PORT' unused: 将其解构出来并在 server 中使用
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
      // 3. 修复 unused vars: 使用配置文件中的端口，而不是写死 8848
      port: VITE_PORT,
      host: "0.0.0.0",
      // 本地跨域代理
      proxy: {
        "/api": {
          target: "http://localhost:8081",
          changeOrigin: true
          // rewrite: (path) => path.replace(/^\/api/, ""),
        }
      },
      // 预热文件
      warmup: {
        clientFiles: ["./index.html", "./src/{views,components}/*"]
      }
    },
    plugins: getPluginsList(VITE_CDN, VITE_COMPRESSION),
    optimizeDeps: {
      include,
      exclude
    },
    build: {
      target: "es2015",
      sourcemap: false,
      chunkSizeWarningLimit: 4000,
      rollupOptions: {
        input: {
          index: pathResolve("./index.html", import.meta.url)
        },
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
});
