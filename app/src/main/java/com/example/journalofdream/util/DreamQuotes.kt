package com.example.journalofdream.util

import android.content.Context

// Цитаты на всех поддерживаемых языках
private val dreamQuotesByLang: Map<String, List<String>> = mapOf(
    "ru" to listOf(
        "Осознанный сон — это дверь в бесконечные возможности твоего разума.",
        "Во сне ты можешь летать. Осознай это — и небо станет твоим.",
        "Каждый сон — это послание от твоего подсознания. Умей его читать.",
        "Тот, кто умеет видеть осознанные сны, живёт две жизни.",
        "Сон — это не отдых. Это путешествие внутрь себя.",
        "Записывай сны — и они станут ярче и богаче.",
        "Граница между сном и реальностью — лишь вопрос осознанности.",
        "Практикуй осознанность днём — и она придёт к тебе ночью.",
        "Твой мозг создаёт целые миры пока ты спишь. Войди в них осознанно.",
        "Лучший способ предсказать будущее — создать его. Даже во сне."
    ),
    "en" to listOf(
        "A lucid dream is a door to infinite possibilities of your mind.",
        "In a dream you can fly. Realize it — and the sky is yours.",
        "Every dream is a message from your subconscious. Learn to read it.",
        "Those who can lucid dream live two lives.",
        "Sleep is not rest. It is a journey into yourself.",
        "Write down your dreams — and they will become brighter and richer.",
        "The boundary between dream and reality is only a matter of awareness.",
        "Practice mindfulness by day — and it will come to you by night.",
        "Your brain creates entire worlds while you sleep. Enter them consciously.",
        "The best way to predict the future is to create it. Even in a dream."
    ),
    "az" to listOf(
        "Şüurlu yuxu — zehninizin sonsuz imkanlarına açılan bir qapıdır.",
        "Yuxuda uça bilərsən. Bunu dərk et — və göy üzü sənin olacaq.",
        "Hər yuxu şüuraltından bir mesajdır. Onu oxumağı öyrən.",
        "Şüurlu yuxu görə bilən insan iki həyat yaşayır.",
        "Yuxu istirahət deyil. Bu, özünüzə doğru bir səyahətdir.",
        "Yuxularını yaz — onlar daha parlaq və zəngin olacaq.",
        "Yuxu ilə gerçəklik arasındakı sərhəd yalnız fərkindalıq məsələsidir.",
        "Gündüz fərkindalıq məşq et — gecə o sənə gələcək.",
        "Yatarkən beynin bütün dünyalar yaradır. Onlara şüurlu daxil ol.",
        "Gələcəyi proqnozlaşdırmanın ən yaxşı yolu onu yaratmaqdır. Hətta yuxuda belə."
    ),
    "de" to listOf(
        "Ein luzider Traum ist eine Tür zu den unendlichen Möglichkeiten deines Geistes.",
        "Im Traum kannst du fliegen. Erkenne es — und der Himmel gehört dir.",
        "Jeder Traum ist eine Botschaft deines Unterbewusstseins. Lerne sie zu lesen.",
        "Wer luzide träumen kann, lebt zwei Leben.",
        "Schlaf ist keine Ruhe. Es ist eine Reise in dein Inneres.",
        "Schreibe deine Träume auf — und sie werden klarer und reicher.",
        "Die Grenze zwischen Traum und Realität ist nur eine Frage des Bewusstseins.",
        "Übe Achtsamkeit am Tag — und sie wird nachts zu dir kommen.",
        "Dein Gehirn erschafft ganze Welten während du schläfst. Betritt sie bewusst.",
        "Der beste Weg die Zukunft vorherzusagen ist, sie zu gestalten. Sogar im Traum."
    ),
    "fr" to listOf(
        "Le rêve lucide est une porte vers les possibilités infinies de votre esprit.",
        "Dans un rêve, vous pouvez voler. Réalisez-le — et le ciel vous appartient.",
        "Chaque rêve est un message de votre subconscient. Apprenez à le lire.",
        "Celui qui sait faire des rêves lucides vit deux vies.",
        "Le sommeil n'est pas du repos. C'est un voyage en soi-même.",
        "Notez vos rêves — et ils deviendront plus vifs et plus riches.",
        "La frontière entre le rêve et la réalité n'est qu'une question de conscience.",
        "Pratiquez la pleine conscience le jour — et elle viendra à vous la nuit.",
        "Votre cerveau crée des mondes entiers pendant que vous dormez. Entrez-y consciemment.",
        "La meilleure façon de prédire l'avenir est de le créer. Même en rêve."
    ),
    "es" to listOf(
        "El sueño lúcido es una puerta a las posibilidades infinitas de tu mente.",
        "En un sueño puedes volar. Darte cuenta de eso — y el cielo será tuyo.",
        "Cada sueño es un mensaje de tu subconsciente. Aprende a leerlo.",
        "Quien sabe soñar lúcidamente vive dos vidas.",
        "El sueño no es descanso. Es un viaje hacia tu interior.",
        "Escribe tus sueños — y se volverán más vívidos y ricos.",
        "La frontera entre el sueño y la realidad es solo una cuestión de conciencia.",
        "Practica la atención plena de día — y vendrá a ti de noche.",
        "Tu cerebro crea mundos enteros mientras duermes. Entra en ellos conscientemente.",
        "La mejor manera de predecir el futuro es crearlo. Incluso en un sueño."
    ),
    "pt" to listOf(
        "O sonho lúcido é uma porta para as possibilidades infinitas da sua mente.",
        "Num sonho você pode voar. Perceba isso — e o céu será seu.",
        "Cada sonho é uma mensagem do seu subconsciente. Aprenda a lê-la.",
        "Quem sabe ter sonhos lúcidos vive duas vidas.",
        "O sono não é descanso. É uma viagem para dentro de si mesmo.",
        "Anote seus sonhos — e eles ficarão mais vívidos e ricos.",
        "A fronteira entre o sonho e a realidade é apenas uma questão de consciência.",
        "Pratique a atenção plena durante o dia — e ela virá até você à noite.",
        "Seu cérebro cria mundos inteiros enquanto você dorme. Entre neles conscientemente.",
        "A melhor maneira de prever o futuro é criá-lo. Mesmo em um sonho."
    ),
    "tr" to listOf(
        "Lucid rüya, zihninizin sonsuz olanaklarına açılan bir kapıdır.",
        "Rüyada uçabilirsin. Bunu fark et — ve gökyüzü senin olacak.",
        "Her rüya bilinçaltından bir mesajdır. Okumayı öğren.",
        "Lucid rüya görebilen iki hayat yaşar.",
        "Uyku dinlenme değildir. Kendinize doğru bir yolculuktur.",
        "Rüyalarını yaz — daha parlak ve zengin hale gelecekler.",
        "Rüya ile gerçeklik arasındaki sınır yalnızca farkındalık meselesidir.",
        "Gündüz farkındalık uygula — gece sana gelecek.",
        "Uyurken beynin tüm dünyalar yaratır. Onlara bilinçli gir.",
        "Geleceği tahmin etmenin en iyi yolu onu yaratmaktır. Rüyada bile."
    ),
    "ar" to listOf(
        "الحلم الواعي هو باب إلى إمكانيات لا نهاية لها في عقلك.",
        "في الحلم يمكنك الطيران. أدرك ذلك — والسماء ستكون لك.",
        "كل حلم هو رسالة من عقلك الباطن. تعلّم قراءتها.",
        "من يعرف أحلام اليقظة يعيش حياتين.",
        "النوم ليس راحة. إنه رحلة إلى أعماق نفسك.",
        "دوّن أحلامك — وستصبح أكثر إشراقاً وثراءً.",
        "الحدود بين الحلم والواقع مجرد مسألة وعي.",
        "مارس اليقظة الذهنية نهاراً — وستأتي إليك ليلاً.",
        "يخلق عقلك عوالم بأكملها أثناء نومك. ادخلها بوعي.",
        "أفضل طريقة للتنبؤ بالمستقبل هي صنعه. حتى في الحلم."
    ),
    "ko" to listOf(
        "자각몽은 당신 마음의 무한한 가능성으로 향하는 문입니다.",
        "꿈속에서 당신은 날 수 있습니다. 그것을 깨달으면 하늘이 당신의 것이 됩니다.",
        "모든 꿈은 잠재의식으로부터의 메시지입니다. 읽는 법을 배우세요.",
        "자각몽을 꿀 수 있는 사람은 두 개의 삶을 삽니다.",
        "수면은 휴식이 아닙니다. 자신 안으로의 여행입니다.",
        "꿈을 기록하세요 — 더 선명하고 풍부해질 것입니다.",
        "꿈과 현실의 경계는 오직 의식의 문제입니다.",
        "낮에 마음챙김을 연습하세요 — 밤에 찾아올 것입니다.",
        "당신이 자는 동안 뇌는 세계를 만듭니다. 의식적으로 들어가세요.",
        "미래를 예측하는 가장 좋은 방법은 만드는 것입니다. 꿈속에서도요."
    ),
    "ja" to listOf(
        "明晰夢はあなたの心の無限の可能性への扉です。",
        "夢の中では飛ぶことができます。それに気づけば空はあなたのものになります。",
        "すべての夢は潜在意識からのメッセージです。読み取ることを学びましょう。",
        "明晰夢を見られる人は二つの人生を生きています。",
        "睡眠は休息ではありません。自分自身への旅です。",
        "夢を書き留めてください — より鮮明で豊かになります。",
        "夢と現実の境界は意識の問題に過ぎません。",
        "昼間にマインドフルネスを練習してください — 夜に訪れます。",
        "あなたが眠っている間、脳は世界を作り出しています。意識的に入りましょう。",
        "未来を予測する最良の方法はそれを作ることです。夢の中でさえも。"
    ),
    "nl" to listOf(
        "Een lucide droom is een deur naar de eindeloze mogelijkheden van je geest.",
        "In een droom kun je vliegen. Realiseer het je — en de hemel is van jou.",
        "Elke droom is een boodschap van je onderbewustzijn. Leer het te lezen.",
        "Wie lucide kan dromen leeft twee levens.",
        "Slaap is geen rust. Het is een reis naar jezelf.",
        "Schrijf je dromen op — en ze worden helderder en rijker.",
        "De grens tussen droom en werkelijkheid is slechts een kwestie van bewustzijn.",
        "Beoefen mindfulness overdag — en het komt 's nachts naar je toe.",
        "Je hersenen creëren hele werelden terwijl je slaapt. Betreed ze bewust.",
        "De beste manier om de toekomst te voorspellen is die te creëren. Zelfs in een droom."
    ),
    "pl" to listOf(
        "Świadomy sen to drzwi do nieskończonych możliwości twojego umysłu.",
        "We śnie możesz latać. Uświadom to sobie — a niebo będzie twoje.",
        "Każdy sen to wiadomość od twojej podświadomości. Naucz się ją czytać.",
        "Ten, kto potrafi śnić świadomie, żyje dwoma życiami.",
        "Sen to nie odpoczynek. To podróż w głąb siebie.",
        "Zapisuj sny — staną się jaśniejsze i bogatsze.",
        "Granica między snem a rzeczywistością to tylko kwestia świadomości.",
        "Ćwicz uważność w ciągu dnia — a przyjdzie do ciebie w nocy.",
        "Twój mózg tworzy całe światy gdy śpisz. Wejdź w nie świadomie.",
        "Najlepszym sposobem przewidywania przyszłości jest jej tworzenie. Nawet we śnie."
    ),
    "it" to listOf(
        "Il sogno lucido è una porta verso le infinite possibilità della tua mente.",
        "In un sogno puoi volare. Renditi conto — e il cielo sarà tuo.",
        "Ogni sogno è un messaggio dal tuo subconscio. Impara a leggerlo.",
        "Chi sa fare sogni lucidi vive due vite.",
        "Il sonno non è riposo. È un viaggio dentro se stessi.",
        "Scrivi i tuoi sogni — diventeranno più vividi e ricchi.",
        "Il confine tra sogno e realtà è solo una questione di consapevolezza.",
        "Pratica la consapevolezza di giorno — e verrà da te di notte.",
        "Il tuo cervello crea interi mondi mentre dormi. Entra in essi consapevolmente.",
        "Il modo migliore per prevedere il futuro è crearlo. Anche in un sogno."
    ),
    "zh" to listOf(
        "清醒梦是通往你心灵无限可能的大门。",
        "在梦中你可以飞翔。意识到这一点——天空就是你的。",
        "每个梦都是来自你潜意识的信息。学会读懂它。",
        "会做清醒梦的人活着两段人生。",
        "睡眠不是休息。这是一段向内的旅程。",
        "记录你的梦——它们会变得更加生动丰富。",
        "梦与现实之间的界限只是意识的问题。",
        "白天练习正念——它会在夜晚来到你身边。",
        "你睡觉时大脑在创造整个世界。有意识地进入它们。",
        "预测未来的最好方法是创造它。即使在梦中也是如此。"
    )
)

// Публичный список встроенных цитат (русские) — используется в QuotesAdminScreen
val dreamQuotes: List<String> = dreamQuotesByLang["ru"]!!

// Возвращает случайную цитату на языке устройства, fallback на английский
fun getRandomQuote(context: Context): String {
    val lang = context.resources.configuration.locales[0].language
    val quotes = dreamQuotesByLang[lang] ?: dreamQuotesByLang["en"]!!
    return quotes.random()
}

// Совместимость со старым кодом без контекста — возвращает английскую
@Deprecated("Используй getRandomQuote(context)")
fun getRandomQuote(): String = dreamQuotesByLang["en"]!!.random()
