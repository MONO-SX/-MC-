package com.tungsten.fcl.shell

/**
 * 整合包更新接入点（MyZ 壳子）。
 *
 * 目前尚未拿到整合包 manifest 地址，因此 [checkUpdate] 恒定回调 null（视为已是最新），
 * [downloadUpdate] 也不会被调用。接入时只需补齐 [MANIFEST_URL] 与这两个方法的实现，
 * 上层 [ShellLaunchController] 的状态机无需改动。
 *
 * 注意：这里不伪造下载进度，也不发起任何网络请求。
 */
object ModpackUpdater {

    // TODO: 待接入整合包 manifest URL（例如 https://example.com/modpack/manifest.json）
    private const val MANIFEST_URL: String = ""

    /**
     * 检查整合包是否有更新。
     * @param callback result=null 表示已是最新；非 null 表示远端存在更新的版本标识。
     */
    fun checkUpdate(callback: (String?) -> Unit) {
        if (MANIFEST_URL.isBlank()) {
            callback(null)
            return
        }
        callback(null)
    }

    /**
     * 下载整合包更新。
     * @param onProgress 进度回调，取值 0..100。
     * @param onFinished 完成回调，true 表示可以继续启动游戏。
     */
    fun downloadUpdate(onProgress: (Int) -> Unit, onFinished: (Boolean) -> Unit) {
        // 真实下载逻辑尚未接入：直接回调失败，避免伪装进度
        onFinished(false)
    }
}
