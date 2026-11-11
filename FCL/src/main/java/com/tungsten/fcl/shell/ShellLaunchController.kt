package com.tungsten.fcl.shell

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.tungsten.fcl.R

/**
 * 「开始游戏」按钮的启动流程控制器（MyZ 壳子）。
 *
 * 状态机：准备就绪 → （需要更新时）版本更新稍后...{百分比}% → 准备就绪。
 * 服务器 IP 不在这里处理，仍由 [ShellServerDefaults] 在组装启动参数时默认注入。
 *
 * @param onLaunch 检查（及必要的更新）完成后真正启动游戏的入口。
 */
class ShellLaunchController(
    private val activity: Activity,
    private val status: TextView,
    private val progress: ProgressBar,
    private val onLaunch: () -> Unit
) {

    private var busy = false

    fun start() {
        if (busy) return
        busy = true
        idle()
        ModpackUpdater.checkUpdate { remote ->
            activity.runOnUiThread {
                if (remote == null) {
                    busy = false
                    onLaunch()
                } else {
                    updateThenLaunch()
                }
            }
        }
    }

    private fun updateThenLaunch() {
        progress.progress = 0
        progress.visibility = View.VISIBLE
        status.text = activity.getString(R.string.shell_status_update, 0)
        ModpackUpdater.downloadUpdate(
            onProgress = { percent ->
                activity.runOnUiThread {
                    progress.progress = percent
                    status.text = activity.getString(R.string.shell_status_update, percent)
                }
            },
            onFinished = { success ->
                activity.runOnUiThread {
                    busy = false
                    idle()
                    if (success) onLaunch()
                }
            }
        )
    }

    /** 回到初始态：文字「准备就绪」、进度条隐藏并清零 */
    private fun idle() {
        progress.progress = 0
        progress.visibility = View.GONE
        status.text = activity.getString(R.string.shell_status_ready)
    }
}
