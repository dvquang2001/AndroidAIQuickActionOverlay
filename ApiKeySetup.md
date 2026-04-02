# Gemini API Key Setup

1. Create a Gemini API key in Google AI Studio:
   `https://aistudio.google.com/app/apikey`

2. Configure one of these sources:
   `local.properties`
   `GEMINI_API_KEY=your_actual_api_key_here`
   `GEMINI_MODEL=gemini-2.5-flash`

   `gradle.properties`
   `GEMINI_API_KEY=your_actual_api_key_here`
   `GEMINI_MODEL=gemini-2.5-flash`

   Environment variable
   `GEMINI_API_KEY=your_actual_api_key_here`
   `GEMINI_MODEL=gemini-2.5-flash`

3. The app exposes that value through `BuildConfig.GEMINI_API_KEY` from [app/build.gradle.kts](/D:/AndroidStudioProjects/QuickActionOverlay/app/build.gradle.kts).

4. Do not commit secrets. Keep `local.properties` out of version control.

5. TODO if the app still shows a quota error:
   Enable billing or increase Gemini API quota for the Google AI Studio project behind your API key.

6. If Gemini returns `429 RESOURCE_EXHAUSTED`, the key is authenticated but the project quota or billing limit has been reached.
