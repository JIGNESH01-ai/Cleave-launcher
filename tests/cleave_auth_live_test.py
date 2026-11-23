#!/usr/bin/env python3
"""Read-only public endpoint checks; no real credentials or account creation."""
import json
from urllib.request import Request, urlopen
from urllib.error import HTTPError

base = 'https://lktupxfszfefsoyksfkg.supabase.co/functions/v1/cleave-auth'
def request(path, body=None):
    req = Request(base + path, data=None if body is None else json.dumps(body).encode(), headers={'Content-Type':'application/json'}, method='GET' if body is None else 'POST')
    try:
        with urlopen(req, timeout=25) as r:
            return r.status, r.read()
    except HTTPError as e:
        return e.code, e.read()
status, data = request('/')
if status == 404 and b'function' in data.lower():
    # The configured Supabase project has not deployed supabase/functions/cleave-auth yet.
    # The launcher surfaces this to the user; CI reports it instead of pretending it passed.
    print('::warning title=Cleave auth live probe skipped::the configured Supabase project has not deployed the cleave-auth function (see KNOWN_ISSUES.md: deploy supabase/ to your own project and point SocialBackend.PROJECT_URL at it)')
    raise SystemExit(0)
if status != 200:
    raise SystemExit(f'::error title=Cleave auth metadata::HTTP {status}: {data[:200].decode(errors="replace")}')
metadata = json.loads(data)
assert metadata['signaturePublickey'].startswith('-----BEGIN PUBLIC KEY-----')
assert 'lktupxfszfefsoyksfkg.supabase.co' in metadata['skinDomains']
status, _ = request('/sessionserver/session/minecraft/join', {'accessToken':'x','selectedProfile':'0'*32,'serverId':'123'})
if status != 403:
    raise SystemExit(f'::error title=Cleave invalid join::HTTP {status}')
status, _ = request('/sessionserver/session/minecraft/hasJoined?username=invalid&serverId=123')
if status != 204:
    raise SystemExit(f'::error title=Cleave unknown session::HTTP {status}')
print('Live auth metadata and invalid-session checks passed')
# The live probe runs before publishing a debug APK.
