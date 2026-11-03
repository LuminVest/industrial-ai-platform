<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-text">
          <h1 class="app-title">工业智能运维决策平台</h1>
          <div class="header-desc">设备全生命周期智能管理解决方案</div>
        </div>
        <!-- 新增logo，放在标题左侧 -->
        <img src="/logo.png" alt="平台logo" class="header-logo" />
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <el-tabs 
        v-model="activeTab" 
        type="border-card"
        class="main-tabs"
        @tab-click="handleTabClick"
      >
        <!-- 工业文档管理标签页 -->
        <el-tab-pane label="工业文档管理" name="document">
          <div class="tab-content">
            <div class="upload-section">
              <el-select 
                v-model="currentDevice" 
                placeholder="请选择设备类型" 
                class="device-select"
              >
                <el-option label="风机" value="fan" />
                <el-option label="水泵" value="pump" />
                <el-option label="电机" value="motor" />
              </el-select>
              <el-upload
                class="upload-demo"
                action="http://localhost:8080/api/document/upload"
                :on-success="onUploadSuccess"
                :headers="headers"
                :data="{deviceType: currentDevice}"
                drag
                :auto-upload="true"
              >
                <div class="el-upload__text">
                  点击或拖拽文件到此处上传<br>
                  <small class="upload-tip">支持PDF、Word、Txt等格式的设备手册、运维文档</small>
                </div>
              </el-upload>
            </div>
            
            <div class="table-container">
              <el-table 
                :data="documents" 
                border 
                stripe
                :header-cell-style="{background: '#f8f9fa'}"
                :row-class-name="tableRowClassName"
                style="width: 100%;"
              >
                <el-table-column prop="fileName" label="文档名称" min-width="200" />
                <el-table-column prop="deviceType" label="设备类型" width="120">
                  <template #default="scope">
                    <el-tag :type="getDeviceTagType(scope.row.deviceType)">{{ getDeviceTagName(scope.row.deviceType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="uploadTime" label="上传时间" width="180" />
                <el-table-column label="操作" width="100">
                  <template #default="scope">
                    <el-button 
                      type="danger" 
                      size="small"
                      @click="deleteDocument(scope.row.id)"
                    >删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 智能运维问答标签页 -->
        <el-tab-pane label="智能运维问答" name="chat">
          <div class="tab-content">
            <div class="chat-header">
              <el-select 
                v-model="chatDevice" 
                placeholder="请选择设备类型" 
                class="device-select"
              >
                <el-option label="风机" value="fan" />
                <el-option label="水泵" value="pump" />
                <el-option label="电机" value="motor" />
              </el-select>
              <el-button 
                type="text" 
                @click="clearChatHistory"
                class="clear-chat-btn"
              >清空对话</el-button>
            </div>
            
            <div class="chat-container">
              <div class="chat-messages" ref="chatMessagesRef">
                <div v-if="chatHistory.length === 0" class="empty-chat">
                  <p>开始与智能运维助手对话吧</p>
                  <p class="empty-tip">基于上传的设备文档，为您解答运维相关问题</p>
                </div>
                <div 
                  v-for="msg in chatHistory" 
                  :key="msg.id" 
                  class="message" 
                  :class="msg.role"
                >
                  <div class="message-avatar"></div>
                  <div class="message-content">{{ msg.content }}</div>
                </div>
              </div>
              
              <div class="chat-input">
                <el-input
                  v-model="userMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入你的运维问题...（回车发送）"
                  @keyup.enter="sendMessage"
                  resize="none"
                />
                <el-button 
                  type="primary"
                  @click="sendMessage"
                  class="send-btn"
                  :disabled="!userMessage.trim()"
                >发送</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 运维报告生成标签页 -->
        <el-tab-pane label="运维报告生成" name="report">
          <div class="tab-content">
            <div class="form-group">
              <el-input 
                v-model="deviceId" 
                placeholder="设备ID"
                class="form-input"
              />
              <el-select 
                v-model="reportDevice" 
                placeholder="设备类型" 
                class="form-select"
              >
                <el-option label="风机" value="fan" />
                <el-option label="水泵" value="pump" />
                <el-option label="电机" value="motor" />
              </el-select>
            </div>
            
            <el-input
              v-model="logContent"
              type="textarea"
              :rows="10"
              placeholder="请粘贴设备的运行日志，系统会自动生成运维分析报告..."
              resize="none"
              class="log-textarea"
            />
            
            <el-button 
              type="primary"
              @click="generateReport"
              class="generate-btn"
              :disabled="!logContent.trim()"
            >生成报告</el-button>

            <div class="report-list">
              <div v-if="reports.length === 0" class="empty-list">
                <p>暂无生成的运维报告</p>
              </div>
              <div 
                v-for="report in reports" 
                :key="report.id" 
                class="report-item"
                @mouseenter="hoverReport = report.id"
                @mouseleave="hoverReport = null"
              >
                <div class="report-header">
                  <div class="report-time">{{ report.createTime }}</div>
                  <el-button 
                    type="text"
                    class="download-btn"
                    v-show="hoverReport === report.id"
                  >导出</el-button>
                </div>
                <div class="report-content">{{ report.content }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 故障诊断标签页 -->
        <el-tab-pane label="故障诊断分析" name="fault">
          <div class="tab-content">
            <div class="form-group">
              <el-input 
                v-model="faultDeviceId" 
                placeholder="设备ID"
                class="form-input"
              />
              <el-select 
                v-model="faultDevice" 
                placeholder="设备类型" 
                class="form-select"
              >
                <el-option label="风机" value="fan" />
                <el-option label="水泵" value="pump" />
                <el-option label="电机" value="motor" />
              </el-select>
            </div>
            
            <el-input
              v-model="faultPhenomenon"
              type="textarea"
              :rows="6"
              placeholder="请描述设备的故障现象，比如：风机轴承温度100℃，振动过大..."
              resize="none"
              class="fault-textarea"
            />
            
            <el-button 
              type="primary"
              @click="diagnoseFault"
              class="diagnose-btn"
              :disabled="!faultPhenomenon.trim()"
            >诊断故障</el-button>

            <div class="fault-list">
              <div v-if="faults.length === 0" class="empty-list">
                <p>暂无故障诊断记录</p>
              </div>
              <div 
                v-for="fault in faults" 
                :key="fault.id" 
                class="fault-item"
              >
                <div class="fault-header">
                  <el-tag type="warning" class="fault-tag">故障现象</el-tag>
                </div>
                <div class="fault-phenomenon">{{ fault.faultPhenomenon }}</div>
                
                <div class="fault-header mt-20">
                  <el-tag type="success" class="fault-tag">诊断结果</el-tag>
                </div>
                <div class="diagnose-result">{{ fault.diagnoseResult }}</div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-content">
        <p>© 2025 工业智能运维决策平台 - 版权所有</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 配置axios的基础地址
axios.defaults.baseURL = 'http://localhost:8080'

export default {
  setup() {
    const activeTab = ref('document')
    const sessionId = ref('engineer-' + Math.random().toString(36).substr(2))
    const headers = ref({})
    const hoverReport = ref(null)

    // 文档相关
    const documents = ref([])
    const currentDevice = ref('fan')

    // 对话相关
    const chatHistory = ref([])
    const userMessage = ref('')
    const chatMessagesRef = ref(null)
    const chatDevice = ref('fan')

    // 报告相关
    const deviceId = ref('device001')
    const reportDevice = ref('fan')
    const logContent = ref('')
    const reports = ref([])

    // 故障相关
    const faultDeviceId = ref('device001')
    const faultDevice = ref('fan')
    const faultPhenomenon = ref('')
    const faults = ref([])

    // 加载所有文档
    const loadDocuments = async () => {
      try {
        const res = await axios.get('/api/document/all')
        documents.value = res.data
      } catch (error) {
        ElMessage.error('加载文档失败：' + error.message)
      }
    }

    // 上传成功回调
    const onUploadSuccess = () => {
      ElMessage.success('上传成功，知识库已更新')
      loadDocuments()
    }

    // 删除文档
    const deleteDocument = async (id) => {
      try {
        await ElMessageBox.confirm(
          '确定要删除该文档吗？此操作不可恢复！',
          '删除确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await axios.delete('/api/document/documents/' + id)
        ElMessage.success('删除成功')
        loadDocuments()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + error.message)
        }
      }
    }

    // 发送对话消息
    const sendMessage = async () => {
      if (!userMessage.value.trim()) return
      
      // 先添加用户消息到界面
      const tempMsg = {
        id: Date.now(),
        role: 'user',
        content: userMessage.value
      }
      chatHistory.value.push(tempMsg)
      const msg = userMessage.value
      userMessage.value = ''
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
      
      try {
        // 调用RAG问答接口
        await axios.post('/api/chat/rag', {
          sessionId: sessionId.value,
          userMessage: msg,
          deviceType: chatDevice.value
        })
        // 加载对话历史
        loadChatHistory()
      } catch (error) {
        ElMessage.error('发送消息失败：' + error.message)
        // 移除临时消息
        chatHistory.value.pop()
      }
    }

    // 加载对话历史
    const loadChatHistory = async () => {
      try {
        const res = await axios.get('/api/chat/history/' + sessionId.value)
        chatHistory.value = res.data
        scrollToBottom()
      } catch (error) {
        ElMessage.error('加载对话历史失败：' + error.message)
      }
    }

    // 生成报告
    const generateReport = async () => {
      if (!logContent.value.trim()) {
        ElMessage.warning('请输入设备运行日志')
        return
      }
      
      try {
        await axios.post('/api/operate/generateReport', {
          deviceId: deviceId.value,
          deviceType: reportDevice.value,
          logContent: logContent.value
        })
        ElMessage.success('报告生成成功')
        logContent.value = ''
        // 加载报告
        loadReports(deviceId.value)
      } catch (error) {
        ElMessage.error('生成报告失败：' + error.message)
      }
    }

    // 加载报告
    const loadReports = async (deviceId) => {
      try {
        const res = await axios.get('/api/operate/reports/' + deviceId)
        reports.value = res.data
      } catch (error) {
        ElMessage.error('加载报告失败：' + error.message)
      }
    }

    // 诊断故障
    const diagnoseFault = async () => {
      if (!faultPhenomenon.value.trim()) {
        ElMessage.warning('请描述故障现象')
        return
      }
      
      try {
        const res = await axios.post('/api/operate/diagnose', {
          deviceId: faultDeviceId.value,
          deviceType: faultDevice.value,
          faultPhenomenon: faultPhenomenon.value
        })
        ElMessage.success('诊断完成')
        faultPhenomenon.value = ''
        // 加载故障记录
        loadFaults(faultDeviceId.value)
      } catch (error) {
        ElMessage.error('诊断故障失败：' + error.message)
      }
    }

    // 加载故障记录
    const loadFaults = async (deviceId) => {
      try {
        const res = await axios.get('/api/operate/faults/' + deviceId)
        faults.value = res.data
      } catch (error) {
        ElMessage.error('加载故障记录失败：' + error.message)
      }
    }

    // 辅助方法
    const scrollToBottom = () => {
      nextTick(() => {
        if (chatMessagesRef.value) {
          chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
        }
      })
    }

    const clearChatHistory = () => {
      ElMessageBox.confirm(
        '确定要清空所有对话记录吗？',
        '清空确认',
        {
          type: 'info'
        }
      ).then(async () => {
        chatHistory.value = []
        ElMessage.success('对话记录已清空')
      })
    }

    const handleTabClick = () => {
      scrollTo(0, 0)
    }

    const tableRowClassName = ({ row, rowIndex }) => {
      return rowIndex % 2 === 0 ? 'row-even' : 'row-odd'
    }

    const getDeviceTagType = (type) => {
      const types = {
        fan: 'primary',
        pump: 'success',
        motor: 'warning'
      }
      return types[type] || 'info'
    }

    const getDeviceTagName = (type) => {
      const names = {
        fan: '风机',
        pump: '水泵',
        motor: '电机'
      }
      return names[type] || type
    }

    onMounted(() => {
      loadDocuments()
      loadChatHistory()
      loadReports('device001')
      loadFaults('device001')
    })

    return {
      activeTab,
      headers,
      documents,
      currentDevice,
      chatHistory,
      userMessage,
      chatMessagesRef,
      chatDevice,
      deviceId,
      reportDevice,
      logContent,
      reports,
      faultDeviceId,
      faultDevice,
      faultPhenomenon,
      faults,
      hoverReport,
      onUploadSuccess,
      deleteDocument,
      sendMessage,
      generateReport,
      diagnoseFault,
      clearChatHistory,
      handleTabClick,
      tableRowClassName,
      getDeviceTagType,
      getDeviceTagName
    }
  }
}
</script>

<style scoped>
.app-container {
  --el-color-primary: #c41e3a; /* 校徽红色，和顶部导航栏匹配 */
}
/* 全局样式 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 头部样式 */
.app-header {
  background: linear-gradient(135deg,#980802);
  color: white;
  padding: 20px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  /* 左右两端对齐，撑开铺满整行 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.header-text {
  display: flex;
  flex-direction: column;
}
/* logo尺寸，按需调整宽高 */
.header-logo {
  height: 70px;
  width: auto;
  object-fit: contain;
}

.app-title {
  font-size: 24px;
  margin: 0;
}

.header-desc {
  margin-top: 8px;
  font-size: 14px;
  opacity: 0.9;
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
  width: 100%;
}

.main-tabs {
  --el-tabs-header-text-color: #495057;
  --el-tabs-active-text-color: #980802;
  --el-tabs-border-color: #e9ecef;
  /* 新增border-card激活侧边竖条颜色 */
  --el-color-primary: #980802;
  /* tab底部滑条颜色 */
  --el-tabs-active-bar-color: #980802;
}

.tab-content {
  padding: 24px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-top: 16px;
}

/* 文档上传区域 */
.upload-section {
  margin-bottom: 24px;
}

.device-select {
  width: 180px;
  margin-bottom: 16px;
}

.upload-demo {
  background: #f8f9fa;
  border: 2px dashed #dee2e6;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
}

.upload-demo:hover {
  border-color: #980802;
  background: #f0f7ff;
}

.upload-tip {
  color: #6c757d;
  font-size: 12px;
  margin-top: 8px;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.row-even {
  background-color: #f8f9fa;
}

.row-odd {
  background-color: white;
}

.btn-delete {
  transition: all 0.2s;
}

.btn-delete:hover {
  transform: scale(1.05);
}

/* 聊天界面样式 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.clear-chat-btn {
  color: #6c757d;
  transition: color 0.2s;
}

.clear-chat-btn:hover {
  color: #980802;
}

.chat-container {
  height: 600px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.empty-chat {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #adb5bd;
  text-align: center;
}

.empty-chat p {
  margin: 0;
  font-size: 16px;
}

.empty-tip {
  font-size: 14px;
  margin-top: 8px !important;
  opacity: 0.8;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #e9ecef;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background-color: #980802;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  line-height: 1.5;
}

.message.user .message-content {
  background: #980802;
  color: white;
}

.chat-input {
  display: flex;
  gap: 12px;
}

.chat-input .el-textarea {
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  padding: 0 24px;
}

/* 报告生成样式 */
.form-group {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.form-input {
  width: 220px;
}

.form-select {
  width: 180px;
}

.log-textarea, .fault-textarea {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: border-color 0.2s;
}

.log-textarea:focus, .fault-textarea:focus {
  border-color: #980802;
  box-shadow: 0 0 0 2px rgba(30, 136, 229, 0.1);
}

.generate-btn, .diagnose-btn {
  padding: 10px 32px;
  font-size: 16px;
  border-radius: 8px;
}

.report-list, .fault-list {
  margin-top: 24px;
}

.empty-list {
  padding: 40px;
  text-align: center;
  color: #adb5bd;
  background: #f8f9fa;
  border-radius: 8px;
}

.empty-list p {
  margin: 12px 0 0;
}

.report-item, .fault-item {
  padding: 24px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 16px;
  background: white;
  transition: box-shadow 0.3s ease;
}

.report-item:hover, .fault-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.report-time {
  font-size: 12px;
  color: #6c757d;
}

.download-btn {
  color: #1e88e5;
  opacity: 0.8;
}

.download-btn:hover {
  opacity: 1;
}

.report-content, .diagnose-result {
  white-space: pre-line;
  line-height: 1.6;
  color: #343a40;
}

.fault-header {
  margin-bottom: 8px;
}

.fault-tag {
  border-radius: 4px;
}

.fault-phenomenon {
  padding: 12px;
  background: #fff8f0;
  border-radius: 6px;
  margin-bottom: 16px;
  line-height: 1.5;
}

.mt-20 {
  margin-top: 20px !important;
}

/* 页脚样式 */
.app-footer {
  background-color: #343a40;
  color: #adb5bd;
  padding: 20px 0;
  margin-top: 40px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .main-content {
    padding: 0 10px;
  }
  
  .tab-content {
    padding: 16px;
  }
  
  .form-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .form-input, .form-select {
    width: 100%;
  }
  
  .chat-container {
    height: 400px;
  }
  
  .message {
    max-width: 90%;
  }
}
</style>