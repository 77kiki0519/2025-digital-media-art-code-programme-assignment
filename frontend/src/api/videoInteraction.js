import request from '@/utils/request'

/**
 * 视频互动API（模块三）
 */

// ========== 播放进度 ==========

// 更新播放进度
export function updateVideoProgress(data) {
  return request({
    url: '/video-interaction/progress',
    method: 'post',
    data
  })
}

// 获取播放进度
export function getVideoProgress(videoId, studentId) {
  return request({
    url: `/video-interaction/progress/${videoId}/${studentId}`,
    method: 'get'
  })
}

// 获取学生的所有进度
export function getStudentProgress(studentId) {
  return request({
    url: `/video-interaction/progress/student/${studentId}`,
    method: 'get'
  })
}

// ========== 视频笔记 ==========

// 添加笔记
export function addVideoNote(data) {
  return request({
    url: '/video-interaction/notes',
    method: 'post',
    data
  })
}

// 更新笔记
export function updateVideoNote(id, data) {
  return request({
    url: `/video-interaction/notes/${id}`,
    method: 'put',
    data
  })
}

// 删除笔记
export function deleteVideoNote(id) {
  return request({
    url: `/video-interaction/notes/${id}`,
    method: 'delete'
  })
}

// 获取视频的笔记
export function getVideoNotes(videoId, studentId) {
  return request({
    url: `/video-interaction/notes/${videoId}/${studentId}`,
    method: 'get'
  })
}

// 获取学生的所有笔记
export function getStudentNotes(studentId) {
  return request({
    url: `/video-interaction/notes/student/${studentId}`,
    method: 'get'
  })
}

// ========== 视频弹幕 ==========

// 发送弹幕
export function sendDanmaku(data) {
  return request({
    url: '/video-interaction/danmaku',
    method: 'post',
    data
  })
}

// 获取视频弹幕
export function getVideoDanmaku(videoId) {
  return request({
    url: `/video-interaction/danmaku/${videoId}`,
    method: 'get'
  })
}

// 隐藏弹幕
export function hideDanmaku(id) {
  return request({
    url: `/video-interaction/danmaku/${id}/hide`,
    method: 'put'
  })
}

// ========== 视频小测验 ==========

// 创建视频小测验
export function createVideoQuiz(data) {
  return request({
    url: '/video-interaction/quiz',
    method: 'post',
    data
  })
}

// 获取视频的小测验
export function getVideoQuizzes(videoId) {
  return request({
    url: `/video-interaction/quiz/${videoId}`,
    method: 'get'
  })
}

// 更新小测验
export function updateVideoQuiz(id, data) {
  return request({
    url: `/video-interaction/quiz/${id}`,
    method: 'put',
    data
  })
}

// 删除小测验
export function deleteVideoQuiz(id) {
  return request({
    url: `/video-interaction/quiz/${id}`,
    method: 'delete'
  })
}

// ========== 学习记录 ==========

// 获取学习记录
export function getLearningRecords(studentId, courseId = null) {
  return request({
    url: `/video-interaction/learning-records/${studentId}`,
    method: 'get',
    params: { courseId }
  })
}

// ========== 多语言字幕 ==========

// 创建字幕
export function createSubtitle(data) {
  return request({
    url: '/video-interaction/subtitles',
    method: 'post',
    data
  })
}

// AI翻译字幕
export function translateSubtitle(data) {
  return request({
    url: '/video-interaction/subtitles/translate',
    method: 'post',
    data
  })
}

// 获取视频的所有字幕
export function getVideoSubtitles(videoId) {
  return request({
    url: `/video-interaction/subtitles/video/${videoId}`,
    method: 'get'
  })
}

// 获取指定语言的字幕
export function getSubtitleByLanguage(videoId, language) {
  return request({
    url: `/video-interaction/subtitles/${videoId}/${language}`,
    method: 'get'
  })
}

// 删除字幕
export function deleteSubtitle(id) {
  return request({
    url: `/video-interaction/subtitles/${id}`,
    method: 'delete'
  })
}

// ========== 知识点弹窗 ==========

// 创建知识点
export function createKnowledgePoint(data) {
  return request({
    url: '/video-interaction/knowledge-points',
    method: 'post',
    data
  })
}

// 获取视频的知识点
export function getVideoKnowledgePoints(videoId) {
  return request({
    url: `/video-interaction/knowledge-points/${videoId}`,
    method: 'get'
  })
}

// 更新知识点
export function updateKnowledgePoint(id, data) {
  return request({
    url: `/video-interaction/knowledge-points/${id}`,
    method: 'put',
    data
  })
}

// 删除知识点
export function deleteKnowledgePoint(id) {
  return request({
    url: `/video-interaction/knowledge-points/${id}`,
    method: 'delete'
  })
}

// ========== 笔记AI整理 ==========

// AI自动整理笔记
export function organizeNotes(data) {
  return request({
    url: '/video-interaction/notes/organize',
    method: 'post',
    data
  })
}

