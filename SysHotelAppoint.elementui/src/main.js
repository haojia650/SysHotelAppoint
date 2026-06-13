import ElementPlus from "element-plus";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import "element-plus/dist/index.css";
import "./styles/index.scss";

// 引入ElementPlus中文语言包
import zhCn from "element-plus/dist/locale/zh-cn.mjs";
// 引入Pinia
import { createPinia } from "pinia";
import { createPersistedState } from "pinia-plugin-persistedstate";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

// 导入 DataV 可视化组件（全局注册，完全正确）
import * as dataV from '@kjgl77/datav-vue3';
import '@kjgl77/datav-vue3/dist/style.css';

// 创建应用实例
const app = createApp(App);
// 关闭开发环境下的警告信息
app.config.warnHandler = (msg, vm, trace) => { };
// 注册路由
app.use(router);

// 注册状态管理
const pinia = createPinia();
pinia.use(createPersistedState());
app.use(pinia);

// 注册ElementPlus并配置中文
app.use(ElementPlus, {
  locale: zhCn,
  // 全局配置Message组件
  message: {
    duration: 1000, // 消息提示显示时长（毫秒），默认1000ms
  },
});

// 全局注册 DataV 组件（正确）
app.use(dataV);

// 注册全局自定义组件 + Element图标
function registerComponents(app) {
  // 注册@components下的所有组件
  const components = import.meta.glob("./components/**/*.vue", { eager: true });
  Object.entries(components).forEach(([path, module]) => {
    const componentName = path
      .split("/")
      .pop()
      .replace(/\.\w+$/, "");
    app.component(componentName, module.default);
  });
  // 注册ElementPlus图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }
}

registerComponents(app);

import { RetryConnect } from "./utils/webscoket";
RetryConnect(1);

app.mount("#app");