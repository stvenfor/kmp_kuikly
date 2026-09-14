# Hybrid Feature + Platform Extension modules

We rejected a single fat `shared` and pure layer-only split. Demo business is cut into Feature Modules; native capabilities (permission, share, etc.) use Platform Extension Modules (common API + per-platform impl). This matches large-repo shape while keeping teaching stories clear.
