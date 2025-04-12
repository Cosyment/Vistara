package com.vistara.aestheticwalls.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vistara.aestheticwalls.R
import com.vistara.aestheticwalls.ui.screens.detail.WallpaperDetailScreen
import com.vistara.aestheticwalls.ui.screens.downloads.DownloadsScreen
import com.vistara.aestheticwalls.ui.screens.favorites.FavoritesScreen
import com.vistara.aestheticwalls.ui.screens.home.HomeScreen
import com.vistara.aestheticwalls.ui.screens.live.LiveLibraryScreen
import com.vistara.aestheticwalls.ui.screens.profile.ProfileScreen
import com.vistara.aestheticwalls.ui.screens.static.StaticLibraryScreen

/**
 * 主导航组件
 * 包含底部导航栏和导航宿主
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    // 获取当前导航状态
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断当前是否在主页面
    val isMainScreen = currentRoute in NavDestination.values().map { it.route }

    Box {
        NavHost(
            navController = navController,
            startDestination = NavDestination.Home.route,
            // 只有在主页面才为底部导航栏留出空间
            modifier = if (isMainScreen) Modifier.padding(bottom = 80.dp) else Modifier
        ) {
            composable(NavDestination.Home.route) {
                HomeScreen(
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    },
                    onSearch = { /* 暂时不处理 */ },
                    onBannerClick = { /* 暂时不处理 */ }
                )
            }
            composable(NavDestination.StaticWallpapers.route) {
                StaticLibraryScreen(
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    },
                    onSearchClick = { /* 暂时不处理 */ }
                )
            }
            composable(NavDestination.LiveWallpapers.route) {
                LiveLibraryScreen(
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    },
                    onSearchClick = { /* 暂时不处理 */ }
                )
            }
            composable(NavDestination.Profile.route) {
                ProfileScreen(
                    onFavoritesClick = { navController.navigate("favorites") },
                    onDownloadsClick = { navController.navigate("downloads") },
                    onAutoChangeClick = { /* 暂时不处理 */ },
                    onSettingsClick = { /* 暂时不处理 */ },
                    onFeedbackClick = { /* 暂时不处理 */ },
                    onAboutClick = { /* 暂时不处理 */ },
                    onUpgradeClick = { /* 暂时不处理 */ }
                )
            }

            // 壁纸详情页面
            composable(
                route = "wallpaper/{wallpaperId}",
                arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
            ) {
                WallpaperDetailScreen(
                    onBackPressed = { navController.navigateUp() }
                )
            }

            // 收藏页面
            composable("favorites") {
                FavoritesScreen(
                    onBackPressed = { navController.navigateUp() },
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    }
                )
            }

            // 下载页面
            composable("downloads") {
                DownloadsScreen(
                    onBackPressed = { navController.navigateUp() },
                    onWallpaperClick = { wallpaper ->
                        navController.navigate("wallpaper/${wallpaper.id}")
                    }
                )
            }
        }

        // 底部导航栏，只在主页面显示
        if (isMainScreen) {
            BottomNavBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
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
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(
                icon = {
                    when (destination) {
                        NavDestination.Home -> {
                            if (selected) {
                                Icon(Icons.Filled.Home, contentDescription = destination.title)
                            } else {
                                Icon(Icons.Outlined.Home, contentDescription = destination.title)
                            }
                        }
                        NavDestination.StaticWallpapers -> {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_image),
                                contentDescription = destination.title
                            )
                        }
                        NavDestination.LiveWallpapers -> {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_movie),
                                contentDescription = destination.title
                            )
                        }
                        NavDestination.Profile -> {
                            if (selected) {
                                Icon(Icons.Filled.Person, contentDescription = destination.title)
                            } else {
                                Icon(Icons.Outlined.Person, contentDescription = destination.title)
                            }
                        }
                    }
                },
                label = { Text(destination.title) },
                selected = selected,
                onClick = {
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
                }
            )
        }
    }
}

/**
 * 导航目的地枚举
 */
enum class NavDestination(val route: String, val title: String) {
    Home("home", "首页"),
    StaticWallpapers("static", "静态"),
    LiveWallpapers("live", "动态"),
    Profile("profile", "我的")
}
