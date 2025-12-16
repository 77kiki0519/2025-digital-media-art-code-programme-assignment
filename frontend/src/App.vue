<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background-color: #f5f7fa;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}

/* 全局卡片样式优化 */
.el-card {
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}

.el-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.el-card__header {
  border-bottom: 1px solid #f0f2f5;
  padding: 18px 20px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(to bottom, #fafbfc, #fff);
}

/* 按钮样式优化 */
.el-button {
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}

.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
}

.el-button--primary:active {
  transform: translateY(0);
}

/* 输入框样式优化 */
.el-input__wrapper {
  border-radius: 6px;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.el-input__wrapper:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 表格样式优化 */
.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-table th {
  background: linear-gradient(to bottom, #fafbfc, #f5f7fa) !important;
  color: #606266;
  font-weight: 600;
}

.el-table tbody tr:hover > td {
  background-color: #f5f7fa !important;
}

/* 标签样式优化 */
.el-tag {
  border-radius: 4px;
  padding: 4px 12px;
  font-weight: 500;
}

/* 分页样式优化 */
.el-pagination {
  font-weight: 500;
}

.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

/* 空状态优化 */
.el-empty {
  padding: 60px 0;
}

.el-empty__description {
  color: #909399;
  font-size: 14px;
}

/* 下拉菜单优化 */
.el-dropdown-menu {
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid #e4e7ed;
  padding: 8px;
}

.el-dropdown-menu__item {
  border-radius: 6px;
  margin: 2px 0;
  transition: all 0.2s;
}

.el-dropdown-menu__item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

/* 对话框优化 */
.el-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.el-dialog__header {
  background: linear-gradient(to bottom, #fafbfc, #fff);
  border-bottom: 1px solid #f0f2f5;
  padding: 20px;
}

.el-dialog__title {
  font-weight: 600;
  color: #303133;
}

/* 消息提示优化 */
.el-message {
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border: 1px solid #e4e7ed;
}

/* 加载动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 页面切换动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
  transition: all 0.3s;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .el-card {
    border-radius: 8px;
  }
  
  .el-card__header {
    padding: 14px 16px;
  }
}
</style>

