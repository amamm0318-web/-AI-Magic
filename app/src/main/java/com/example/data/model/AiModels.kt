package com.example.data.model

import java.util.UUID

enum class AiModelOption(
  val id: String,
  val displayName: String,
  val descriptionAr: String,
  val supportsThinking: Boolean = false,
  val isRecommended: Boolean = false
) {
  GEMINI_3_8_FLASH(
    id = "gemini-3.8-flash",
    displayName = "Gemini 3.8 Flash",
    descriptionAr = "المحرك الأساسي: متوازن، سريع وذكي لجميع الاستخدامات",
    supportsThinking = false,
    isRecommended = true
  ),
  GEMINI_4_ARGON(
    id = "gemini-4-argon",
    displayName = "Gemini 4 Argon",
    descriptionAr = "المحرك الأقوى: تحليل متقدم، تفكير عالي وبرمجة عميقة",
    supportsThinking = true,
    isRecommended = false
  ),
  GEMINI_3_1_FLASH_LITE(
    id = "gemini-3.1-flash-lite",
    displayName = "Gemini 3.1 Flash Lite",
    descriptionAr = "المحرك فائق السرعة: إجابات فورية وخفيفة",
    supportsThinking = false,
    isRecommended = false
  )
}

data class AiRole(
  val id: String,
  val titleAr: String,
  val iconEmoji: String,
  val systemPrompt: String
) {
  companion object {
    val presets = listOf(
      AiRole(
        id = "general",
        titleAr = "مساعد ذكي شامل",
        iconEmoji = "🤖",
        systemPrompt = "أنت مساعد ذكاء اصطناعي متقدم وودود، تجيب بلغة عربية فصيحة ومتقنة مع تنظيم الأفكار بنقاط واضحة وتنسيق Markdown احترافي."
      ),
      AiRole(
        id = "programmer",
        titleAr = "مهندس برمجة",
        iconEmoji = "💻",
        systemPrompt = "أنت مهندس برمجيات أول ومطور أنظمة خبير. تقدم حلولاً برمجية نظيفة مع شرح الخوارزميات وأفضل الممارسات."
      ),
      AiRole(
        id = "writer",
        titleAr = "كاتب إبداعي",
        iconEmoji = "✍️",
        systemPrompt = "أنت كاتب محتوى إبداعي وسيناريست ومؤلف بارع، تصيغ الأفكار والنصوص بأسلوب مشوق وجذاب وبلاغة لغوية عالية."
      ),
      AiRole(
        id = "translator",
        titleAr = "مترجم ومدقق",
        iconEmoji = "🌐",
        systemPrompt = "أنت مترجم فوري ومراجع لغوي محترف بين العربية والإنجليزية وسائر اللغات، مع الحفاظ على روح النص والسياق الثقافي."
      ),
      AiRole(
        id = "teacher",
        titleAr = "معلم مبسط",
        iconEmoji = "🎓",
        systemPrompt = "أنت معلم ملهم يشرح أعقد المفاهيم الرياضية والعلمية بأسلوب بسيط وتشبيهات ملموسة وخطوة بخطوة."
      )
    )
  }
}

enum class MessageSender {
  USER, AI
}

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val text: String,
  val sender: MessageSender,
  val timestamp: Long = System.currentTimeMillis(),
  val modelName: String? = null,
  val isThinking: Boolean = false,
  val imageBase64: String? = null,
  val isStreaming: Boolean = false
)

enum class AspectRatioOption(val label: String, val apiValue: String, val ratioFloat: Float) {
  SQUARE_1_1("1:1 مربع", "1:1", 1.0f),
  LANDSCAPE_16_9("16:9 شاشة عريضة", "16:9", 16f / 9f),
  PORTRAIT_9_16("9:16 ستوري وعمودي", "9:16", 9f / 16f),
  LANDSCAPE_4_3("4:3 تقليدي", "4:3", 4f / 3f),
  PORTRAIT_3_4("3:4 تصوير عمودي", "3:4", 3f / 4f),
  PORTRAIT_2_3("2:3 بوستر", "2:3", 2f / 3f),
  LANDSCAPE_3_2("3:2 فوتوغرافي", "3:2", 3f / 2f),
  CINEMATIC_21_9("21:9 سينمائي", "21:9", 21f / 9f)
}

enum class ImageResolutionOption(val label: String, val resolutionPx: Int) {
  SIZE_1K("1K (1024×1024)", 1024),
  SIZE_2K("2K (2048×2048)", 2048),
  SIZE_4K("4K (4096×4096)", 4096)
}

data class GeneratedImageItem(
  val id: String = UUID.randomUUID().toString(),
  val prompt: String,
  val base64Data: String,
  val aspectRatio: AspectRatioOption,
  val timestamp: Long = System.currentTimeMillis(),
  val filePath: String? = null
)

enum class VisionAnalysisMode(val titleAr: String, val promptInstruction: String) {
  GENERAL(
    "تحليل شامل وتفاصيل ذكية",
    "حلل هذه الصورة تحليلاً دقيقاً ومفصلاً. اذكر العناصر الرئيسية، الألوان، السياق، والملاحظات البارزة باللغة العربية."
  ),
  OCR(
    "استخراج وقراءة النصوص (OCR)",
    "استخرج جميع النصوص المكتوبة في هذه الصورة بدقة شديدة مع المحافظة على الترتيب واللغة، واكتبها كنص مقروء."
  ),
  CREATIVE(
    "أفكار ووصف إبداعي",
    "صف المشهد في هذه الصورة بأسلوب شاعري وإبداعي، واقترح عنواناً ملهماً وقصة قصيرة مستوحاة منها."
  ),
  CUSTOM(
    "سؤال حر حول الصورة",
    ""
  )
}
