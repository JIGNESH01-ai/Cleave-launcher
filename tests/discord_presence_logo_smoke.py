"""Check the Rich Presence image points to the shipped logo in both link modes."""
import hashlib
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
base = root / 'app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/cleave/discord'
src = (base / 'CleaveDiscordPresence.kt').read_text()
url = re.search(r'const val CLEAVE_LOGO_URL\s*=\s*"([^"]+)"', src).group(1)
assert url == ('https://raw.githubusercontent.com/JIGNESH01-ai/Cleave-launcher/'
               '22e3001635ab345ed21fe48823093f8cada56f8c/assets/cleave-logo.png')
logo = (root / 'assets/cleave-logo.png').read_bytes()
assert hashlib.sha256(logo).hexdigest() == '96f178bc7c01c3bc622b2cf86437a6753a87434cbec090a6f7585d6efab38f06'
assert logo == (root / 'app_pojavlauncher/src/main/res/drawable-nodpi/cleave_logo.png').read_bytes()
activity = src.split('private fun sendActivity()', 1)[1].split('private fun send(frame:', 1)[0]
assets, links = activity.split('val assets = JSONObject()', 1)[1].split('if (clickable) {', 1)
assert 'assets.put("large_image", CLEAVE_LOGO_URL)' in assets
assert 'assets.put("small_image", CLEAVE_LOGO_URL)' in assets
assert 'clickable && inGameMode && isValidHttpAssetUrl(it)' in assets
assert 'activity.put("assets", assets)' in links
preview = (base / 'CleaveDiscordPresenceCard.kt').read_text().split('fun DiscordRichPresenceLivePreview', 1)[1]
assert 'painterResource(R.drawable.cleave_logo)' in preview
print('Discord presence logo wiring passed')
