#!/usr/bin/env python3
"""Guard the original Cleave layout, live actions, and shared glass treatment."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = root / 'app_pojavlauncher/src/main'
ui = main / 'java/net/kdt/pojavlaunch/cleave/ui'
shell = (ui / 'CleaveLauncherScreen.kt').read_text()
nav = (ui / 'CleaveNavigationRail.kt').read_text()
home = (ui / 'screens/HomeScreen.kt').read_text()
discover = (ui / 'screens/DiscoverScreen.kt').read_text()
accounts = (ui / 'screens/AccountsScreen.kt').read_text()
settings = (ui / 'screens/SettingsScreen.kt').read_text()
game_menu = (ui / 'game/GameMenuScreen.kt').read_text()
settings_components = (main / 'java/net/kdt/pojavlaunch/cleave/components/MiraiSettingsComponents.kt').read_text()
client_components = (main / 'java/net/kdt/pojavlaunch/cleave/client/LiquidCrystalComponents.kt').read_text()
focus_scroll = (main / 'java/net/kdt/pojavlaunch/cleave/components/BringFocusedItemIntoView.kt').read_text()
glass = (main / 'java/net/kdt/pojavlaunch/cleave/components/LiquidGlass.kt').read_text()
surfaces = (main / 'java/net/kdt/pojavlaunch/cleave/theme/CleaveSurface.kt').read_text()
for section in ('HOME', 'LIBRARY', 'DISCOVER', 'CLEAVE_CLIENT', 'SOCIALS', 'SETTINGS', 'ACCOUNTS', 'INSTANCE_EDITOR'):
    assert 'LauncherSection.' + section in shell, section
assert 'CleaveNavigationRail(' in shell and 'HomeScreen(activity, navigate)' in shell
assert 'GlassHomeScreen' not in shell and 'CleaveBottomNavigation' not in shell
assert 'LauncherSection.CLEAVE_CLIENT' in nav and 'LauncherSection.CREATE_INSTANCE' in nav
for action in ('PojavLaunchBridge.play(activity)', 'PojavInstanceBridge.edit', 'PojavNavigator.openCustomControls',
               'LauncherSection.CREATE_INSTANCE', 'LauncherSection.FILES', 'LauncherSection.LOGS'):
    assert action in home, action
assert 'CompactSkinStageCard' not in home
for category in ('MODS', 'MODPACKS', 'RESOURCE_PACKS', 'SHADERS', 'WORLDS'):
    assert category in discover, category
assert 'PojavAccountBridge.select(account)' in accounts and 'PojavAccountBridge.delete(account)' in accounts
assert 'CleaveNav.TAB_STYLES -> FontStylesTab(activity)' in settings
assert 'itemsIndexed(items)' in settings_components and 'heightIn(max = 280.dp)' in settings_components
assert '.selectable(selected = selected, role = Role.RadioButton' in settings_components
assert '.bringFocusedItemIntoView()' in settings_components and 'requester.bringIntoView()' in focus_scroll
assert '.bringFocusedItemIntoView()' in nav
assert '.bringFocusedItemIntoView()' in game_menu and '.bringFocusedItemIntoView()' in client_components
assert 'drawOutline(outline = outline, brush = sheen)' in glass
assert 'drawOutline(outline = outline, brush = rim' in glass
assert 'val accentDefault = Color(0xFF8DEFE0)' in surfaces # The old Cleave accent stays intact.
assert (main / 'assets/wallpapers/cleave_twilight.jpg').is_file() # Keep previously chosen wallpaper valid.
assert 'cleave_twilight' in (main / 'java/net/kdt/pojavlaunch/cleave/theme/CleaveThemeManager.kt').read_text()
assert 'DEFAULT_WALLPAPER = "wp_01_lush_caves"' in (main / 'java/net/kdt/pojavlaunch/cleave/bridge/CleavePreferences.kt').read_text()
print('Original Cleave navigation, live actions and improved glass: smoke checks passed')
