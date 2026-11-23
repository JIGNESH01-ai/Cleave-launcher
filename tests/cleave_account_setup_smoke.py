#!/usr/bin/env python3
"""Wiring checks for the launcher account page and the browser PKCE handoff."""
from pathlib import Path
root = Path(__file__).resolve().parents[1] / 'app_pojavlauncher/src/main/java/net/kdt/pojavlaunch'
home = (root/'cleave/ui/screens/HomeScreen.kt').read_text()
page = (root/'cleave/ui/screens/CleaveAccountSetupScreen.kt').read_text()
cosmetics = (root/'cleave/ui/screens/SocialProfileScreen.kt').read_text()
nav = (root/'cleave/ui/CleaveLauncherScreen.kt').read_text()
backend = (root/'cleave/social/SocialBackend.kt').read_text()
assert home.count('CleaveAccountHomeCard(accent, actions.cleaveAccount)') == 2
assert 'Sign in to Cleave account' in home
assert 'LauncherSection.CLEAVE_ACCOUNT -> CleaveAccountSetupScreen' in nav
assert 'beginGoogleSignIn(activity, true)' in page and 'ProfileCosmeticsPage(activity, setupFlow = true' in page
assert 'claimed != null && setupFlow' in cosmetics
assert 'oauth_pkce_verifier' in backend and 'code_challenge_method=s256' in backend
assert 'grant_type=pkce' in backend and 'auth_code' in backend and 'code_verifier' in backend
callback = backend.split('fun completeBrowserSession(',1)[1].split('suspend fun resendConfirmation',1)[0]
assert 'getQueryParameter("code")' in callback and 'access_token"]' not in callback
assert 'remove("oauth_pkce_verifier")' in callback
print('Cleave home account page and PKCE sign-in wiring passed')
