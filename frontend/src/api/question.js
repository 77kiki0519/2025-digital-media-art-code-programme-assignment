import request from '@/utils/request'

/**
 * 创建提问
 */
export function createQuestion(data) {
  return request({
    url: '/questions',
    method: 'post',
    data
  })
}

/**
 * AI回答问题
 */
export function answerWithAI(id, params) {
  return request({
    url: `/questions/${id}/answer-ai`,
    method: 'post',
    params
  })
}

/**
 * 获取课程问题列表
 */
export function getCourseQuestions(courseId, params) {
  return request({
    url: `/questions/course/${courseId}`,
    method: 'get',
    params
  })
}

/**
 * 获取学生问题列表
 */
export function getStudentQuestions(studentId, params) {
  return request({
    url: `/questions/student/${studentId}`,
    method: 'get',
    params
  })
}

/**
 * 获取聊天历史
 */
export function getChatHistory(studentId, courseId, limit = 50) {
  return request({
    url: `/questions/history/${studentId}`,
    method: 'get',
    params: { courseId, limit }
  })
}

/**
 * 保存AI回答
 */
export function saveAnswer(questionId, content) {
  return request({
    url: `/questions/${questionId}/save-answer`,
    method: 'post',
    data: { content }
  })
}


