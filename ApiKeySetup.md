# ── Gemini API Key Setup ───────────────────────────────────────────────────────
#
# 1. Lấy API key tại: https://aistudio.google.com/app/apikey
#
# 2. Thêm vào local.properties (KHÔNG commit file này lên git):
#    GEMINI_API_KEY=your_actual_api_key_here
#
# 3. Trong build.gradle.kts, expose key qua BuildConfig:
#
#    android {
#        defaultConfig {
#            val key = project.findProperty("GEMINI_API_KEY")?.toString() ?: ""
#            buildConfigField("String", "GEMINI_API_KEY", "\"$key\"")
#        }
#    }
#
# 4. Dùng trong code:
#    BuildConfig.GEMINI_API_KEY
#
# ─────────────────────────────────────────────────────────────────────────────
# QUAN TRỌNG: Thêm dòng này vào .gitignore:
#   local.properties
# ─────────────────────────────────────────────────────────────────────────────