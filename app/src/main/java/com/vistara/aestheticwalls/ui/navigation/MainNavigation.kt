package com.vistara.aestheticwalls.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.data.model.BannerActionType
import com.vistara.aestheticwalls.ui.screens.about.AboutScreen
import com.vistara.aestheticwalls.ui.screens.auth.AuthScreen
import com.vistara.aestheticwalls.ui.screens.autochange.AutoChangeScreen
import com.vistara.aestheticwalls.ui.screens.detail.WallpaperDetailScreen
import com.vistara.aestheticwalls.ui.screens.downloads.DownloadsScreen
import com.vistara.aestheticwalls.ui.screens.edit.WallpaperEditScreen
import com.vistara.aestheticwalls.ui.screens.favorites.FavoritesScreen
import com.vistara.aestheticwalls.ui.screens.feedback.FeedbackScreen
import com.vistara.aestheticwalls.ui.screens.home.HomeScreen
import com.vistara.aestheticwalls.ui.screens.lives.LiveLibraryScreen
import com.vistara.aestheticwalls.ui.screens.mine.MineScreen
import com.vistara.aestheticwalls.ui.screens.premium.PremiumScreen
import com.vistara.aestheticwalls.ui.screens.recharge.RechargeScreen
import com.vistara.aestheticwalls.ui.screens.search.SearchScreen
import com.vistara.aestheticwalls.ui.screens.settings.SettingsScreen
import com.vistara.aestheticwalls.ui.screens.statics.StaticLibraryScreen
import com.vistara.aestheticwalls.ui.screens.test.ApiTestScreen
import com.vistara.aestheticwalls.ui.screens.test.TestScreen
import com.vistara.aestheticwalls.ui.screens.webview.WebViewScreen
import com.vistara.aestheticwalls.ui.theme.LocalAppResources

/**
 * 主导航组件
 * 包含底部导航栏和导航宿主
 */
@Composable
fun MainNavigation(navController: NavHostController = rememberNavController()) {

    // 获取当前导航状态
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断当前是否在主页面
    val isMainScreen = currentRoute in NavDestination.values().map { it.route }

    Box {
        NavHost(
            navController = navController, startDestination = NavDestination.Home.route,
            // 只有在主页面才为底部导航栏留出空间
            modifier = if (isMainScreen) Modifier.padding(bottom = 80.dp) else Modifier
        ) {
            // 登录页面
            composable("auth") {
                AuthScreen(onLoginSuccess = {
                    navController.popBackStack()
                }, onSkipLogin = {
                    navController.popBackStack()
                })
            }
            composable(NavDestination.Home.route) {
                HomeScreen(onWallpaperClick = { wallpaper ->
                    navController.navigate("wallpaper/${wallpaper.id}")
                }, onSearch = { query ->
                    navController.navigate("search?query=$query")
                }, onBannerClick = { banner ->
                    when (banner.actionType) {
                        BannerActionType.WALLPAPER, BannerActionType.COLLECTION -> {
                            // 跳转到壁纸详情页
                            banner.actionTarget?.let { wallpaperId ->
                                navController.navigate("wallpaper/$wallpaperId")
                            }
                        }

//                        BannerActionType.COLLECTION -> {
//                            // 暂时不处理专题跳转，可以在后续实现
//                        }

                        BannerActionType.PREMIUM -> {
                            // 跳转到会员页面
                            navController.navigate("premium")
                        }

                        BannerActionType.URL -> {
                            // 暂时不处理外部URL跳转，可以在后续实现
                        }
                    }
                })
            }
            composable(NavDestination.StaticWallpapers.route) {
                StaticLibraryScreen(onWallpaperClick = { wallpaper ->
                    navController.navigate("wallpaper/${wallpaper.id}")
                }, onSearchClick = {
                    navController.navigate("search")
                })
            }
            composable(NavDestination.LiveWallpapers.route) {
                LiveLibraryScreen(onWallpaperClick = { wallpaper ->
                    navController.navigate("wallpaper/${wallpaper.id}")
                }, onSearchClick = {
                    navController.navigate("search")
                })
            }
            composable(NavDestination.Mine.route) {
                val context = LocalContext.current
                MineScreen(
                    onFavoritesClick = { navController.navigate("favorites") },
                    onDownloadsClick = { navController.navigate("downloads") },
                    onAutoChangeClick = { navController.navigate("autochange") },
                    onSettingsClick = { navController.navigate("settings") },
                    onFeedbackClick = { navController.navigate("feedback") },
                    onAboutClick = { navController.navigate("about") },
                    onUpgradeClick = { navController.navigate("premium") },
                    onLoginClick = { navController.navigate("auth") },
                    onDiamondClick = { navController.navigate("diamond") },
                    onTestToolsClick = {
                        // 导航到测试工具页面
                        navController.navigate("test")
                    })
            }

            // 升级页面
            composable("premium") {
                PremiumScreen(
                    onBackPressed = { navController.navigateUp() },
                    onUpgradeSuccess = { navController.navigateUp() },
                    navController = navController
                )
            }

            // 钻石充值页面
            composable("diamond") {
                RechargeScreen(
                    onBackPressed = { navController.navigateUp() },
                    navController = navController)
            }

            // 搜索页面
            composable(
                route = "search?query={query}", arguments = listOf(navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query") ?: ""
                SearchScreen(onWallpaperClick = { wallpaper ->
                    navController.navigate("wallpaper/${wallpaper.id}")
                }, onBackClick = { navController.navigateUp() })
            }

            // 壁纸编辑页面
            composable(
                route = "edit/{wallpaperId}",
                arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
            ) {
                val wallpaperId = it.arguments?.getString("wallpaperId") ?: ""

                WallpaperEditScreen(
                    onBackPressed = { navController.navigateUp() },
                    onSaveComplete = {
                        // 返回详情页面
                        navController.navigateUp()
                    })
            }

            // 壁纸详情页面
            composable(
                route = "wallpaper/{wallpaperId}",
                arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
            ) {
                WallpaperDetailScreen(
                    onBackPressed = { navController.navigateUp() },
                    onNavigateToEdit = { wallpaperId ->
                        navController.navigate("edit/$wallpaperId")
                    },
                    onNavigateToUpgrade = {
                        navController.navigate("premium")
                    },
                    onNavigateToLogin = {
                        navController.navigate("auth")
                    },
                    onNavigateToDiamondRecharge = {
                        navController.navigate("diamond")
                    })
            }

            // 收藏页面
            composable("favorites") {
                FavoritesScreen(
                    onBackPressed = { navController.navigateUp() },
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    },
                    onNavigateToLogin = { navController.navigate("auth") })
            }

            // 下载页面
            composable("downloads") {
                DownloadsScreen(
                    onBackPressed = { navController.navigateUp() },
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    },
                    onNavigateToLogin = { navController.navigate("auth") })
            }

            // 设置页面
            composable("settings") {
                SettingsScreen(
                    onBackPressed = { navController.navigateUp() })
            }

            // 自动更换壁纸页面
            composable("autochange") {
                AutoChangeScreen(
                    onBackPressed = { navController.navigateUp() },
                    onNavigateToLogin = { navController.navigate("auth") })
            }

            // 评分与反馈页面
            composable("feedback") {
                FeedbackScreen(
                    onBackPressed = { navController.navigateUp() })
            }

            // 关于页面
            composable("about") {
                AboutScreen(
                    onBackPressed = { navController.navigateUp() }, navController = navController
                )
            }

            // WebView页面
            composable(
                route = "webview?url={url}", arguments = listOf(navArgument("url") {
                    type = NavType.StringType
                    nullable = false
                }, navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""

                // 解码URL和标题
                val url = java.net.URLDecoder.decode(encodedUrl, "UTF-8")
                val title = java.net.URLDecoder.decode(encodedTitle, "UTF-8")

                WebViewScreen(
                    url = url, title = title, onBackPressed = { navController.navigateUp() })
            }

            // 测试工具页面
            composable("test") {
                TestScreen(
                    onBackPressed = { navController.navigateUp() },
                    onNavigateToApiTest = { navController.navigate("test/api") })
            }

            // API测试页面
            composable("test/api") {
                ApiTestScreen(
                    onBackPressed = { navController.navigateUp() })
            }
        }

        // 底部导航栏，只在主页面显示
        if (isMainScreen) {
            BottomNavBar(
                navController = navController, modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavBar(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        NavDestination.values().forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(icon = {
                when (destination) {
                    NavDestination.Home -> {
                        if (selected) {
                            Icon(Icons.Filled.Home, contentDescription = destination.getTitle())
                        } else {
                            Icon(
                                Icons.Outlined.Home, contentDescription = destination.getTitle()
                            )
                        }
                    }

                    NavDestination.StaticWallpapers -> {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.ic_image),
                            contentDescription = destination.getTitle()
                        )
                    }

                    NavDestination.LiveWallpapers -> {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.ic_movie),
                            contentDescription = destination.getTitle()
                        )
                    }

                    NavDestination.Mine -> {
                        if (selected) {
                            Icon(Icons.Filled.Person, contentDescription = destination.getTitle())
                        } else {
                            Icon(Icons.Outlined.Person, contentDescription = destination.getTitle())
                        }
                    }
                }
            }, label = { Text(destination.getTitle()) }, selected = selected, onClick = {
                navController.navigate(destination.route) {
                    // 避免创建多个实例
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    // 避免重复点击
                    launchSingleTop = true
                    // 恢复状态
                    restoreState = true
                }
            })
        }
    }
}

/**
 * 导航目的地枚举
 */
enum class NavDestination(val route: String, val titleResId: Int) {
    Home("home", R.string.nav_home) {
        @Composable
        override fun getTitle(): String {
            // 使用 LocalAppResources 确保语言变化时能正确更新
            val resources = LocalAppResources.current
            return resources.getString(R.string.home)
        }
    },
    StaticWallpapers("static", R.string.nav_static) {
        @Composable
        override fun getTitle(): String {
            val resources = LocalAppResources.current
            return resources.getString(R.string.category_static)
        }
    },
    LiveWallpapers("live", R.string.nav_live) {
        @Composable
        override fun getTitle(): String {
            val resources = LocalAppResources.current
            return resources.getString(R.string.category_live)
        }
    },
    Mine("mine", R.string.nav_mine) {
        @Composable
        override fun getTitle(): String {
            val resources = LocalAppResources.current
            return resources.getString(R.string.mine)
        }
    };

    @Composable
    abstract fun getTitle(): String
}
