/*
 * This file is part of Fold Craft Launcher (mc.xxx.com shell fork).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tungsten.fcl.shell

import com.tungsten.fcl.setting.VersionSetting
import com.tungsten.fclcore.util.StringUtils

/**
 * mc.xxx.com 专用客户端的默认服务器补全。
 *
 * 只在参数拼装层提供默认值，不修改实例配置，也不写回 [VersionSetting.serverIp]，
 * 因此每次启动的判定结果一致，重复启动不会累积参数。
 *
 * 判定顺序（任一命中即放弃补全）：
 * 1. 用户已在实例设置里填写服务器地址 → 原样使用；
 * 2. 用户自定义游戏参数里已出现 --server / --port / --quickPlay* → 尊重用户参数；
 * 3. 以上都没有 → 使用默认地址 [DEFAULT_ADDRESS]。
 *
 * 版本分支（--server/--port 还是 --quickPlayMultiplayer）由
 * DefaultLauncher#generateCommandLine 按游戏版本决定，本类不参与。
 */
object ShellServerDefaults {
    /** 默认服务器域名，后续换成真实域名只需改这里（或改为 BuildConfig 字段）。 */
    const val DEFAULT_HOST: String = "mc.your-server.com"

    /** 默认端口。 */
    const val DEFAULT_PORT: Int = 25565

    /** 传给 LaunchOptions 的默认地址。 */
    val DEFAULT_ADDRESS: String = "$DEFAULT_HOST:$DEFAULT_PORT"

    /** 会与默认值冲突、出现即表示用户自己接管了服务器参数的选项。 */
    private val SERVER_ARGUMENT_KEYS = listOf("--server", "--port", "--quickPlay")

    /**
     * 计算本次启动应传给 LaunchOptions 的服务器地址。
     *
     * @return 用户填写的地址、空串（不注入）、或默认地址
     */
    @JvmStatic
    fun resolveServerIp(setting: VersionSetting): String {
        val configured = setting.serverIp
        if (StringUtils.isNotBlank(configured))
            return configured
        if (declaresServerArgument(setting.minecraftArgs))
            return ""
        return DEFAULT_ADDRESS
    }

    /** 自定义游戏参数里是否已声明服务器相关选项（按 token 精确匹配，避免误判路径或模组参数）。 */
    private fun declaresServerArgument(minecraftArgs: String): Boolean {
        if (StringUtils.isBlank(minecraftArgs))
            return false
        return StringUtils.tokenize(minecraftArgs).any { token ->
            SERVER_ARGUMENT_KEYS.any { key ->
                token.equals(key, ignoreCase = true) || token.startsWith("$key=", ignoreCase = true)
            }
        }
    }
}
