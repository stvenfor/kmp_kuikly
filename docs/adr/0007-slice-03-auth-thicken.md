# Slice-03: Auth thicken — OTP ↔ password switch on Login

**Status:** accepted (recommended defaults 2026-09-16)  
**Parent:** ADR-0005  
**Spec:** `.scratch/my-ai-migration/slice-03/spec.md`

## Decision

Thicken product Login (Compose) to match Migration Source **single-page credential switch**: 手机验证码 ↔ 邮箱/密码. Both Mock paths succeed and honor `LoginRedirect` / Me resume. Footer register/forgot remain later-slice affordances. No new Auth Gradle module; stay in `feature-auth` + `core-data`.

## Consequences

- Goldens add login-otp and login-password modes; three-platform gate.
- Register / standalone OTP pages / full footer deep links out of this slice.
