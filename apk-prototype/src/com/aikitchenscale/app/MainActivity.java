package com.aikitchenscale.app;

import android.app.Activity;
import android.content.Intent;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int PICK_PHOTO = 1001;
    private static final int TAKE_PHOTO = 1002;
    private static final int MODE_SCALE = 1;
    private static final int MODE_ESTIMATE = 2;
    private int selectedMode = MODE_SCALE;
    private LinearLayout page;
    private EditText weightInput;
    private EditText oilInput;
    private Spinner cookingSpinner;
    private Spinner foodSpinner;
    private Spinner weightUnitSpinner;
    private Spinner oilUnitSpinner;
    private Uri selectedPhoto;
    private TextView recognitionStatus;
    private boolean foodConfirmed;
    private String language = "en";

    private static final String[] LANGUAGE_CODES = {"en", "mk", "sq", "ja", "es", "de", "fr", "it", "pt"};
    private static final String[] LANGUAGE_NAMES = {"English", "Македонски", "Shqip", "日本語", "Español", "Deutsch", "Français", "Italiano", "Português"};
    private static final Map<String, String[]> TRANSLATIONS = new HashMap<String, String[]>();
    static {
        TRANSLATIONS.put("choose_mode", new String[]{"Choose how you want to analyze your meal.","Изберете како сакате да го анализирате оброкот.","Zgjidhni si dëshironi ta analizoni vaktin.","食事の分析方法を選択してください。","Elige cómo quieres analizar tu comida.","Wähle, wie du deine Mahlzeit analysieren möchtest.","Choisissez comment analyser votre repas.","Scegli come analizzare il tuo pasto.","Escolha como deseja analisar a refeição."});
        TRANSLATIONS.put("scale_mode", new String[]{"Food on kitchen scale","Храна на кујнска вага","Ushqim në peshore kuzhine","キッチンスケール上の食品","Comida en báscula de cocina","Lebensmittel auf Küchenwaage","Aliment sur balance de cuisine","Cibo sulla bilancia da cucina","Alimento na balança de cozinha"});
        TRANSLATIONS.put("estimate_mode", new String[]{"Photo without scale","Фотографија без вага","Foto pa peshore","スケールなしの写真","Foto sin báscula","Foto ohne Waage","Photo sans balance","Foto senza bilancia","Foto sem balança"});
        TRANSLATIONS.put("language", new String[]{"Language","Јазик","Gjuha","言語","Idioma","Sprache","Langue","Lingua","Idioma"});
        TRANSLATIONS.put("select_language", new String[]{"Select language","Изберете јазик","Zgjidhni gjuhën","言語を選択","Seleccionar idioma","Sprache auswählen","Choisir la langue","Seleziona lingua","Selecionar idioma"});
        TRANSLATIONS.put("continue", new String[]{"Continue","Продолжи","Vazhdo","続ける","Continuar","Weiter","Continuer","Continua","Continuar"});
        TRANSLATIONS.put("photo_scale", new String[]{"Photo with scale","Фотографија со вага","Foto me peshore","スケール付き写真","Foto con báscula","Foto mit Waage","Photo avec balance","Foto con bilancia","Foto com balança"});
        TRANSLATIONS.put("photo_no_scale", new String[]{"Photo without scale","Фотографија без вага","Foto pa peshore","スケールなしの写真","Foto sin báscula","Foto ohne Waage","Photo sans balance","Foto senza bilancia","Foto sem balança"});
        TRANSLATIONS.put("take_photo", new String[]{"Take photo","Фотографирај","Bëj foto","写真を撮る","Tomar foto","Foto aufnehmen","Prendre une photo","Scatta foto","Tirar foto"});
        TRANSLATIONS.put("gallery", new String[]{"Choose from gallery","Избери од галерија","Zgjidh nga galeria","ギャラリーから選択","Elegir de la galería","Aus Galerie wählen","Choisir dans la galerie","Scegli dalla galleria","Escolher da galeria"});
        TRANSLATIONS.put("back", new String[]{"Back","Назад","Prapa","戻る","Atrás","Zurück","Retour","Indietro","Voltar"});
        TRANSLATIONS.put("confirm_details", new String[]{"Confirm meal details","Потврди ги деталите за оброкот","Konfirmo detajet e vaktit","食事の詳細を確認","Confirmar detalles de la comida","Mahlzeitdetails bestätigen","Confirmer les détails du repas","Conferma i dettagli del pasto","Confirmar detalhes da refeição"});
        TRANSLATIONS.put("confirm_food", new String[]{"Confirm selected food","Потврди ја избраната храна","Konfirmo ushqimin e zgjedhur","選択した食品を確認","Confirmar alimento seleccionado","Ausgewähltes Lebensmittel bestätigen","Confirmer l’aliment sélectionné","Conferma alimento selezionato","Confirmar alimento selecionado"});
        TRANSLATIONS.put("manual_weight_notice", new String[]{"Enter the portion weight yourself; photo analysis currently identifies food only.","Внесете ја грамажата сами; анализата на фотографијата засега препознава само храна.","Shkruani vetë peshën; analiza e fotos tani njeh vetëm ushqimin.","重さは自分で入力してください。写真解析は現在、食品の識別のみ対応します。","Introduce el peso; el análisis de fotos solo identifica alimentos por ahora.","Gewicht selbst eingeben; die Fotoanalyse erkennt derzeit nur Lebensmittel.","Saisissez le poids ; l’analyse photo reconnaît seulement les aliments pour le moment.","Inserisci il peso; l’analisi della foto per ora identifica solo gli alimenti.","Insira o peso; a análise da foto atualmente só identifica alimentos."});
        TRANSLATIONS.put("weight_source_scale", new String[]{"Weight entered from your scale","Тежина внесена од вашата вага","Pesha e shënuar nga peshorja juaj","はかりから入力した重さ","Peso introducido de tu báscula","Gewicht von Ihrer Waage eingegeben","Poids saisi depuis votre balance","Peso inserito dalla bilancia","Peso inserido da sua balança"});
        TRANSLATIONS.put("weight_source_manual", new String[]{"Weight entered by you; the photo did not measure it","Тежината ја внесовте вие; фотографијата не ја измери","Peshën e shënuat ju; fotografia nuk e mati","入力した重さです。写真から測定していません","Peso introducido por ti; la foto no lo midió","Gewicht von Ihnen eingegeben; das Foto hat es nicht gemessen","Poids saisi par vous ; la photo ne l’a pas mesuré","Peso inserito da te; la foto non lo ha misurato","Peso inserido por si; a foto não o mediu"});
        TRANSLATIONS.put("weight", new String[]{"Weight","Тежина","Pesha","重量","Peso","Gewicht","Poids","Peso","Peso"});
        TRANSLATIONS.put("preparation", new String[]{"Preparation","Подготовка","Përgatitja","調理方法","Preparación","Zubereitung","Préparation","Preparazione","Preparação"});
        TRANSLATIONS.put("oil", new String[]{"Added oil","Додадено масло","Vaj i shtuar","追加した油","Aceite añadido","Zugegebenes Öl","Huile ajoutée","Olio aggiunto","Óleo adicionado"});
        TRANSLATIONS.put("calculate", new String[]{"Calculate nutrition","Пресметај нутритивни вредности","Llogarit vlerat ushqyese","栄養を計算","Calcular nutrición","Nährwerte berechnen","Calculer la nutrition","Calcola nutrizione","Calcular nutrição"});
        TRANSLATIONS.put("result", new String[]{"Nutrition result","Нутритивен резултат","Rezultati ushqyes","栄養結果","Resultado nutricional","Nährwert-Ergebnis","Résultat nutritionnel","Risultato nutrizionale","Resultado nutricional"});
        TRANSLATIONS.put("again", new String[]{"Analyze another meal","Анализирај друг оброк","Analizo një vakt tjetër","別の食事を分析","Analizar otra comida","Weitere Mahlzeit analysieren","Analyser un autre repas","Analizza un altro pasto","Analisar outra refeição"});
        TRANSLATIONS.put("daily", new String[]{"Today's nutrition","Денешна исхрана","Ushqimi i sotëm","今日の栄養","Nutrición de hoy","Heutige Ernährung","Nutrition du jour","Nutrizione di oggi","Nutrição de hoje"});
        TRANSLATIONS.put("save_meal", new String[]{"Save to daily log","Зачувај во дневник","Ruaj në ditar","日記に保存","Guardar en el diario","Im Tagesprotokoll speichern","Enregistrer dans le journal","Salva nel diario","Salvar no diário"});
        TRANSLATIONS.put("saved", new String[]{"Meal saved","Оброкот е зачуван","Vakti u ruajt","食事を保存しました","Comida guardada","Mahlzeit gespeichert","Repas enregistré","Pasto salvato","Refeição salva"});
        TRANSLATIONS.put("meals", new String[]{"Meals","Оброци","Vakte","食事","Comidas","Mahlzeiten","Repas","Pasti","Refeições"});
        TRANSLATIONS.put("goal", new String[]{"My goal","Моја цел","Qëllimi im","目標","Mi objetivo","Mein Ziel","Mon objectif","Il mio obiettivo","Meu objetivo"});
    }

    private final String[] foods = {"Chicken breast", "White rice", "Potato", "Egg", "Apple", "Banana", "Salmon", "Mixed salad", "Beef", "Pork", "Turkey", "Tuna", "Shrimp", "Pasta", "Oats", "Bread", "Cheese", "Yogurt", "Milk", "Tomato", "Cucumber", "Carrot", "Broccoli", "Onion", "Orange", "Strawberry", "Grapes", "Avocado", "Beans", "Lentils"};
    private final double[][] nutrients = {
        {165, 31.0, 3.6, 0.0, 0.0, 0.0, 256, 29, 1.0, 1.0, 0.6, 0.3},
        {130, 2.7, 0.3, 28.2, 0.4, 0.1, 35, 12, 0.2, 0.5, 0.1, 0.0},
        {87, 1.9, 0.1, 20.1, 1.8, 0.9, 379, 22, 0.3, 0.3, 0.3, 0.0},
        {155, 12.6, 10.6, 1.1, 0.0, 1.1, 126, 12, 1.2, 1.1, 0.1, 1.1},
        {52, 0.3, 0.2, 13.8, 2.4, 10.4, 107, 5, 0.1, 0.0, 0.0, 0.0},
        {89, 1.1, 0.3, 22.8, 2.6, 12.2, 358, 27, 0.3, 0.2, 0.4, 0.0},
        {208, 20.4, 13.4, 0.0, 0.0, 0.0, 363, 27, 0.3, 0.6, 0.6, 3.2},
        {25, 1.5, 0.3, 4.5, 2.0, 2.5, 220, 14, 0.7, 0.3, 0.1, 0.0},
        {250, 26.0, 15.0, 0.0, 0.0, 0.0, 318, 21, 2.6, 6.3, 0.4, 2.5},
        {242, 27.0, 14.0, 0.0, 0.0, 0.0, 423, 28, 1.0, 2.4, 0.5, 0.7},
        {135, 29.0, 1.8, 0.0, 0.0, 0.0, 239, 30, 1.2, 1.7, 0.8, 1.0},
        {132, 29.0, 0.6, 0.0, 0.0, 0.0, 252, 34, 1.0, 0.8, 0.5, 2.5},
        {99, 24.0, 0.3, 0.2, 0.0, 0.0, 259, 35, 0.5, 1.3, 0.1, 1.1},
        {131, 5.0, 1.1, 25.0, 1.8, 0.6, 44, 18, 1.3, 0.6, 0.1, 0.0},
        {389, 16.9, 6.9, 66.3, 10.6, 0.0, 429, 177, 4.7, 4.0, 0.1, 0.0},
        {265, 9.0, 3.2, 49.0, 2.7, 5.0, 115, 25, 3.6, 0.8, 0.1, 0.0},
        {350, 25.0, 27.0, 1.3, 0.0, 0.5, 98, 28, 0.7, 3.1, 0.1, 1.5},
        {61, 3.5, 3.3, 4.7, 0.0, 4.7, 155, 11, 0.1, 0.6, 0.1, 0.4},
        {50, 3.4, 1.9, 4.8, 0.0, 5.0, 150, 11, 0.0, 0.4, 0.0, 0.5},
        {18, 0.9, 0.2, 3.9, 1.2, 2.6, 237, 11, 0.3, 0.2, 0.1, 0.0},
        {15, 0.7, 0.1, 3.6, 0.5, 1.7, 147, 13, 0.3, 0.2, 0.0, 0.0},
        {41, 0.9, 0.2, 9.6, 2.8, 4.7, 320, 12, 0.3, 0.2, 0.1, 0.0},
        {35, 2.4, 0.4, 7.2, 3.3, 1.4, 316, 21, 0.7, 0.4, 0.2, 0.0},
        {40, 1.1, 0.1, 9.3, 1.7, 4.2, 146, 10, 0.2, 0.2, 0.1, 0.0},
        {47, 0.9, 0.1, 11.8, 2.4, 9.4, 181, 10, 0.1, 0.1, 0.1, 0.0},
        {32, 0.7, 0.3, 7.7, 2.0, 4.9, 153, 13, 0.4, 0.1, 0.0, 0.0},
        {69, 0.7, 0.2, 18.1, 0.9, 15.5, 191, 7, 0.4, 0.1, 0.1, 0.0},
        {160, 2.0, 14.7, 8.5, 6.7, 0.7, 485, 29, 0.6, 0.6, 0.3, 0.0},
        {127, 8.7, 0.5, 22.8, 6.4, 0.3, 405, 42, 2.9, 1.1, 0.1, 0.0},
        {116, 9.0, 0.4, 20.1, 7.9, 1.8, 369, 36, 3.3, 1.3, 0.2, 0.0}
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(247, 251, 248));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        language = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "");
        if (language.length() == 0) showLanguageChoice(true); else showModeChoice();
    }

    private int languageIndex() {
        for (int i = 0; i < LANGUAGE_CODES.length; i++) if (LANGUAGE_CODES[i].equals(language)) return i;
        return 0;
    }

    private String t(String key) {
        String[] values = TRANSLATIONS.get(key);
        return values == null ? key : values[languageIndex()];
    }

    private String mk(String english, String macedonian) {
        if (language.equals("en")) return english;
        if (language.equals("mk")) return macedonian;
        String[][] extra = {
            {"Saktësia më e mirë: fotografoni ushqimin dhe ekranin e peshores së bashku.", "AI-vlerësim: diapazon ±16% — jo peshë fizike e saktë.", "Për rezultat të besueshëm:", "• Tregoni gjithë pjatën\n• Përdorni dritë të mirë\n• Mbajeni ushqimin qartë të dukshëm\n• Fotografoni nga lart", "• Sigurohuni që çdo shifër në peshore të lexohet", "Fotografia nuk u krijua", "Kamera nuk është e disponueshme", "Zgjidhni fillimisht ushqimin", "Konfirmoni fillimisht ushqimin", "Vendosni peshë të vlefshme", "Fotografia nuk u njoh — zgjidhni ushqimin manualisht."},
            {"最も正確にするには、食品とスケール表示を一緒に撮影してください。", "AI推定：±16%の範囲。正確な実測重量ではありません。", "信頼できる結果のために：", "• 皿全体を写す\n• 明るい場所で撮影\n• 食品をはっきり写す\n• 上から撮影", "• スケールの数字をすべて読み取れるようにする", "写真を作成できませんでした", "カメラを利用できません", "先に食品を選択してください", "先に選択した食品を確認してください", "有効な重量を入力してください", "写真を認識できませんでした。食品を手動で選択してください。"},
            {"Mayor precisión: fotografía juntos el alimento y la pantalla de la báscula.", "Estimación de IA: rango ±16%; no es un peso físico exacto.", "Para un resultado fiable:", "• Muestra todo el plato\n• Usa buena luz\n• Mantén visible el alimento\n• Fotografía desde arriba", "• Asegúrate de que se lean todos los dígitos", "No se pudo crear la foto", "La cámara no está disponible", "Selecciona primero un alimento", "Confirma primero el alimento", "Introduce un peso válido", "No se pudo reconocer la foto; selecciona el alimento manualmente."},
            {"Beste Genauigkeit: Lebensmittel und Waagenanzeige zusammen fotografieren.", "KI-Schätzung: ±16 % Bereich – kein exaktes Gewicht.", "Für ein zuverlässiges Ergebnis:", "• Ganzen Teller zeigen\n• Gutes Licht verwenden\n• Lebensmittel klar sichtbar halten\n• Von oben fotografieren", "• Alle Ziffern der Waage müssen lesbar sein", "Foto konnte nicht erstellt werden", "Kamera ist nicht verfügbar", "Zuerst Lebensmittel auswählen", "Ausgewähltes Lebensmittel zuerst bestätigen", "Gültiges Gewicht eingeben", "Foto nicht erkannt – Lebensmittel manuell auswählen."},
            {"Précision optimale : photographiez ensemble l’aliment et l’écran de la balance.", "Estimation IA : plage de ±16 %, pas un poids exact.", "Pour un résultat fiable :", "• Montrez toute l’assiette\n• Utilisez un bon éclairage\n• Gardez l’aliment bien visible\n• Photographiez du dessus", "• Vérifiez que tous les chiffres sont lisibles", "Impossible de créer la photo", "La caméra n’est pas disponible", "Sélectionnez d’abord un aliment", "Confirmez d’abord l’aliment", "Saisissez un poids valide", "Photo non reconnue — sélectionnez l’aliment manuellement."},
            {"Massima precisione: fotografa insieme alimento e display della bilancia.", "Stima AI: intervallo ±16%, non è un peso esatto.", "Per un risultato affidabile:", "• Mostra tutto il piatto\n• Usa una buona luce\n• Mantieni il cibo ben visibile\n• Fotografa dall’alto", "• Assicurati che tutte le cifre siano leggibili", "Impossibile creare la foto", "La fotocamera non è disponibile", "Seleziona prima un alimento", "Conferma prima l’alimento", "Inserisci un peso valido", "Foto non riconosciuta — seleziona manualmente l’alimento."},
            {"Melhor precisão: fotografe o alimento e o visor da balança juntos.", "Estimativa de IA: intervalo ±16%, não é um peso exato.", "Para um resultado confiável:", "• Mostre todo o prato\n• Use boa iluminação\n• Mantenha o alimento visível\n• Fotografe de cima", "• Certifique-se de que todos os dígitos estão legíveis", "Não foi possível criar a foto", "A câmera não está disponível", "Selecione primeiro um alimento", "Confirme primeiro o alimento", "Digite um peso válido", "Foto não reconhecida — selecione o alimento manualmente."}
        };
        String[] englishKeys = {"Best accuracy: photograph the food and the scale display together.", "AI estimate: ±16% range — not an exact physical weight.", "For a reliable result:", "• Show the full plate\n• Use good light\n• Keep the food clearly visible\n• Photograph from above", "• Make sure every digit on the scale display is readable", "Could not create a photo", "Camera is not available", "Select a food first", "Confirm the selected food first", "Enter a valid weight", "Could not recognize this photo — select the food manually."};
        int languageRow = languageIndex() - 2;
        for (int i = 0; i < englishKeys.length; i++) if (englishKeys[i].equals(english)) return extra[languageRow][i];
        return english;
    }

    private String[] cookingMethods() {
        String[][] values = {
            {"Boiled", "Baked", "Fried", "Steamed", "Raw"},
            {"Варено", "Печено", "Пржено", "На пареа", "Сурово"},
            {"E zier", "E pjekur", "E skuqur", "Me avull", "E gjallë"},
            {"茹でる", "焼く", "揚げる", "蒸す", "生"},
            {"Hervido", "Horneado", "Frito", "Al vapor", "Crudo"},
            {"Gekocht", "Gebacken", "Gebraten", "Gedämpft", "Roh"},
            {"Bouilli", "Cuit au four", "Frit", "À la vapeur", "Cru"},
            {"Bollito", "Al forno", "Fritto", "Al vapore", "Crudo"},
            {"Cozido", "Assado", "Frito", "Ao vapor", "Cru"}
        };
        return values[languageIndex()];
    }

    private String[] foodChoices() {
        String[][] values = {
            {"Select food", "Chicken breast", "White rice", "Potato", "Egg", "Apple", "Banana", "Salmon", "Mixed salad", "Beef", "Pork", "Turkey", "Tuna", "Shrimp", "Pasta", "Oats", "Bread", "Cheese", "Yogurt", "Milk", "Tomato", "Cucumber", "Carrot", "Broccoli", "Onion", "Orange", "Strawberry", "Grapes", "Avocado", "Beans", "Lentils"},
            {"Избери храна", "Пилешки гради", "Бел ориз", "Компир", "Јајце", "Јаболко", "Банана", "Лосос", "Мешана салата", "Говедско", "Свинско", "Мисиркино", "Туна", "Ракчиња", "Тестенини", "Овес", "Леб", "Сирење", "Јогурт", "Млеко", "Домат", "Краставица", "Морков", "Брокула", "Кромид", "Портокал", "Јагода", "Грозје", "Авокадо", "Грав", "Леќа"},
            {"Zgjidh ushqimin", "Gjoks pule", "Oriz i bardhë", "Patate", "Vezë", "Mollë", "Banane", "Salmon", "Sallatë e përzier", "Mish viçi", "Mish derri", "Gjel deti", "Ton", "Karkaleca", "Makarona", "Tërshërë", "Bukë", "Djathë", "Kos", "Qumësht", "Domate", "Kastravec", "Karotë", "Brokoli", "Qepë", "Portokall", "Luleshtrydhe", "Rrush", "Avokado", "Fasule", "Thjerrëza"},
            {"食品を選択", "鶏むね肉", "白米", "じゃがいも", "卵", "りんご", "バナナ", "サーモン", "ミックスサラダ", "牛肉", "豚肉", "七面鳥", "マグロ", "エビ", "パスタ", "オーツ麦", "パン", "チーズ", "ヨーグルト", "牛乳", "トマト", "きゅうり", "にんじん", "ブロッコリー", "玉ねぎ", "オレンジ", "いちご", "ぶどう", "アボカド", "豆", "レンズ豆"},
            {"Seleccionar alimento", "Pechuga de pollo", "Arroz blanco", "Patata", "Huevo", "Manzana", "Plátano", "Salmón", "Ensalada mixta", "Carne de res", "Cerdo", "Pavo", "Atún", "Camarones", "Pasta", "Avena", "Pan", "Queso", "Yogur", "Leche", "Tomate", "Pepino", "Zanahoria", "Brócoli", "Cebolla", "Naranja", "Fresa", "Uvas", "Aguacate", "Frijoles", "Lentejas"},
            {"Lebensmittel wählen", "Hähnchenbrust", "Weißer Reis", "Kartoffel", "Ei", "Apfel", "Banane", "Lachs", "Gemischter Salat", "Rindfleisch", "Schweinefleisch", "Pute", "Thunfisch", "Garnelen", "Nudeln", "Haferflocken", "Brot", "Käse", "Joghurt", "Milch", "Tomate", "Gurke", "Karotte", "Brokkoli", "Zwiebel", "Orange", "Erdbeere", "Trauben", "Avocado", "Bohnen", "Linsen"},
            {"Choisir un aliment", "Blanc de poulet", "Riz blanc", "Pomme de terre", "Œuf", "Pomme", "Banane", "Saumon", "Salade composée", "Bœuf", "Porc", "Dinde", "Thon", "Crevettes", "Pâtes", "Avoine", "Pain", "Fromage", "Yaourt", "Lait", "Tomate", "Concombre", "Carotte", "Brocoli", "Oignon", "Orange", "Fraise", "Raisins", "Avocat", "Haricots", "Lentilles"},
            {"Seleziona alimento", "Petto di pollo", "Riso bianco", "Patata", "Uovo", "Mela", "Banana", "Salmone", "Insalata mista", "Manzo", "Maiale", "Tacchino", "Tonno", "Gamberetti", "Pasta", "Avena", "Pane", "Formaggio", "Yogurt", "Latte", "Pomodoro", "Cetriolo", "Carota", "Broccoli", "Cipolla", "Arancia", "Fragola", "Uva", "Avocado", "Fagioli", "Lenticchie"},
            {"Selecionar alimento", "Peito de frango", "Arroz branco", "Batata", "Ovo", "Maçã", "Banana", "Salmão", "Salada mista", "Carne bovina", "Carne de porco", "Peru", "Atum", "Camarão", "Massa", "Aveia", "Pão", "Queijo", "Iogurte", "Leite", "Tomate", "Pepino", "Cenoura", "Brócolis", "Cebola", "Laranja", "Morango", "Uvas", "Abacate", "Feijão", "Lentilhas"}
        };
        return values[languageIndex()];
    }

    private String confirmFoodLabel() {
        String[] values = {"Confirm the food", "Потврди ја храната", "Konfirmo ushqimin", "食品を確認", "Confirmar el alimento", "Lebensmittel bestätigen", "Confirmer l’aliment", "Conferma l’alimento", "Confirmar o alimento"};
        return values[languageIndex()];
    }

    private String aiConfirmationNote() {
        String[] values = {
            "AI only suggests the product. Confirm the food and weight before calculation.",
            "AI само предлага производ. Потврдете ги храната и тежината пред пресметката.",
            "AI vetëm sugjeron produktin. Konfirmoni ushqimin dhe peshën para llogaritjes.",
            "AIは食品を提案するだけです。計算前に食品と重量を確認してください。",
            "La IA solo sugiere el producto. Confirma el alimento y el peso antes del cálculo.",
            "Die KI schlägt das Produkt nur vor. Lebensmittel und Gewicht vor der Berechnung bestätigen.",
            "L’IA suggère seulement le produit. Confirmez l’aliment et le poids avant le calcul.",
            "L’IA suggerisce soltanto il prodotto. Conferma alimento e peso prima del calcolo.",
            "A IA apenas sugere o produto. Confirme o alimento e o peso antes do cálculo."
        };
        return values[languageIndex()];
    }

    private String nutritionNote() {
        String[] values = {
            "Values use an early built-in food profile and remain approximate. Exact values depend on variety, recipe and absorbed oil.",
            "Вредностите се приближни. Точниот резултат зависи од видот, рецептот и впиеното масло.",
            "Vlerat janë të përafërta. Rezultati i saktë varet nga lloji, receta dhe vaji i përthithur.",
            "数値は概算です。正確な値は食材の種類、レシピ、吸収した油の量によって異なります。",
            "Los valores son aproximados. El resultado exacto depende de la variedad, la receta y el aceite absorbido.",
            "Die Werte sind Näherungswerte. Das genaue Ergebnis hängt von Sorte, Rezept und aufgenommenem Öl ab.",
            "Les valeurs sont approximatives. Le résultat exact dépend de la variété, de la recette et de l’huile absorbée.",
            "I valori sono approssimativi. Il risultato esatto dipende dalla varietà, dalla ricetta e dall’olio assorbito.",
            "Os valores são aproximados. O resultado exato depende da variedade, da receita e do óleo absorvido."
        };
        return values[languageIndex()];
    }

    private String[] nutritionLabels() {
        String[][] values = {
            {"Calories", "Protein", "Fat", "Carbohydrates", "Fiber", "Sugars", "Vitamins and minerals", "Potassium", "Magnesium", "Iron", "Zinc", "Equivalent to"},
            {"Калории", "Протеини", "Масти", "Јаглехидрати", "Влакна", "Шеќери", "Витамини и минерали", "Калиум", "Магнезиум", "Железо", "Цинк", "Еквивалентно на"},
            {"Kalori", "Proteina", "Yndyrna", "Karbohidrate", "Fibra", "Sheqerna", "Vitamina dhe minerale", "Kalium", "Magnez", "Hekur", "Zink", "E barabartë me"},
            {"カロリー", "たんぱく質", "脂質", "炭水化物", "食物繊維", "糖類", "ビタミンとミネラル", "カリウム", "マグネシウム", "鉄", "亜鉛", "換算"},
            {"Calorías", "Proteínas", "Grasas", "Carbohidratos", "Fibra", "Azúcares", "Vitaminas y minerales", "Potasio", "Magnesio", "Hierro", "Zinc", "Equivale a"},
            {"Kalorien", "Protein", "Fett", "Kohlenhydrate", "Ballaststoffe", "Zucker", "Vitamine und Mineralstoffe", "Kalium", "Magnesium", "Eisen", "Zink", "Entspricht"},
            {"Calories", "Protéines", "Lipides", "Glucides", "Fibres", "Sucres", "Vitamines et minéraux", "Potassium", "Magnésium", "Fer", "Zinc", "Équivaut à"},
            {"Calorie", "Proteine", "Grassi", "Carboidrati", "Fibre", "Zuccheri", "Vitamine e minerali", "Potassio", "Magnesio", "Ferro", "Zinco", "Equivale a"},
            {"Calorias", "Proteínas", "Gorduras", "Carboidratos", "Fibras", "Açúcares", "Vitaminas e minerais", "Potássio", "Magnésio", "Ferro", "Zinco", "Equivale a"}
        };
        return values[languageIndex()];
    }

    private String photoText(int type) {
        String[][] values = {
            {"Analyzing the photo…", "Enter the exact number visible on the scale", "Photo-based estimate — confirm or correct the weight", "AI server is not active yet — select the food manually."},
            {"Фотографијата се анализира…", "Внесете го точниот број прикажан на вагата", "Проценка од фотографија — потврдете ја или поправете ја тежината", "AI-серверот сè уште не е активен — изберете ја храната рачно."},
            {"Po analizohet fotografia…", "Vendosni numrin e saktë të shfaqur në peshore", "Vlerësim nga fotografia — konfirmoni ose korrigjoni peshën", "Serveri AI nuk është ende aktiv — zgjidhni ushqimin manualisht."},
            {"写真を分析中…", "スケールに表示された正確な数値を入力してください", "写真による推定 — 重量を確認または修正してください", "AIサーバーはまだ有効ではありません。食品を手動で選択してください。"},
            {"Analizando la foto…", "Introduce el número exacto mostrado en la báscula", "Estimación por foto: confirma o corrige el peso", "El servidor de IA aún no está activo: selecciona el alimento manualmente."},
            {"Foto wird analysiert…", "Gib die genaue Zahl auf der Waage ein", "Foto-Schätzung – Gewicht bestätigen oder korrigieren", "Der KI-Server ist noch nicht aktiv – Lebensmittel manuell auswählen."},
            {"Analyse de la photo…", "Saisissez le nombre exact affiché sur la balance", "Estimation par photo — confirmez ou corrigez le poids", "Le serveur IA n’est pas encore actif — sélectionnez l’aliment manuellement."},
            {"Analisi della foto…", "Inserisci il numero esatto mostrato sulla bilancia", "Stima dalla foto — conferma o correggi il peso", "Il server AI non è ancora attivo — seleziona manualmente l’alimento."},
            {"Analisando a foto…", "Digite o número exato mostrado na balança", "Estimativa pela foto — confirme ou corrija o peso", "O servidor de IA ainda não está ativo — selecione o alimento manualmente."}
        };
        return values[languageIndex()][type];
    }

    private String[] aiLabels() {
        String[][] values = {
            {"Food confirmed", "Food is unclear — choose it manually, then confirm.", "AI suggestion", "confidence", "Please confirm or change it.", "AI scans remaining", "Weight confirmed from your scale", "Photo estimate: likely range"},
            {"Храната е потврдена", "Храната не е јасна — изберете ја рачно и потврдете.", "AI-предлог", "сигурност", "Потврдете или променете.", "преостанати AI-скенирања", "Тежината е потврдена од вашата вага", "Проценка од фотографија: веројатен опсег"},
            {"Ushqimi u konfirmua", "Ushqimi nuk është i qartë — zgjidheni manualisht dhe konfirmoni.", "Sugjerim AI", "siguri", "Konfirmoni ose ndryshoni.", "skanime AI të mbetura", "Pesha u konfirmua nga peshorja", "Vlerësim nga fotografia: diapazoni i mundshëm"},
            {"食品を確認しました", "食品が不明です。手動で選択して確認してください。", "AIの提案", "信頼度", "確認または変更してください。", "残りのAIスキャン", "スケールで重量を確認済み", "写真による推定範囲"},
            {"Alimento confirmado", "El alimento no está claro: selecciónalo manualmente y confirma.", "Sugerencia de IA", "confianza", "Confirma o cambia la selección.", "escaneos de IA restantes", "Peso confirmado en tu báscula", "Estimación por foto: rango probable"},
            {"Lebensmittel bestätigt", "Lebensmittel unklar – manuell auswählen und bestätigen.", "KI-Vorschlag", "Sicherheit", "Bitte bestätigen oder ändern.", "verbleibende KI-Scans", "Gewicht durch deine Waage bestätigt", "Foto-Schätzung: wahrscheinlicher Bereich"},
            {"Aliment confirmé", "Aliment incertain — sélectionnez-le manuellement puis confirmez.", "Suggestion IA", "confiance", "Confirmez ou modifiez.", "analyses IA restantes", "Poids confirmé par votre balance", "Estimation par photo : plage probable"},
            {"Alimento confermato", "Alimento non chiaro — selezionalo manualmente e conferma.", "Suggerimento AI", "affidabilità", "Conferma o modifica.", "scansioni AI rimanenti", "Peso confermato dalla bilancia", "Stima dalla foto: intervallo probabile"},
            {"Alimento confirmado", "Alimento incerto — selecione manualmente e confirme.", "Sugestão de IA", "confiança", "Confirme ou altere.", "análises de IA restantes", "Peso confirmado pela balança", "Estimativa pela foto: intervalo provável"}
        };
        return values[languageIndex()];
    }

    private void showLanguageChoice(final boolean firstLaunch) {
        createPage();
        page.addView(text("AI Kitchen Scale", 29, dark(), true));
        page.addView(text(t("select_language"), 21, dark(), true), margins(0, 20, 0, 12));
        final Spinner languageSpinner = new Spinner(this);
        languageSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, LANGUAGE_NAMES));
        languageSpinner.setSelection(languageIndex());
        page.addView(languageSpinner, sizeMargins(-1, dp(58), 0, 0, 0, 22));
        Button save = actionButton(t("continue"));
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                language = LANGUAGE_CODES[languageSpinner.getSelectedItemPosition()];
                getSharedPreferences("settings", MODE_PRIVATE).edit().putString("language", language).apply();
                showModeChoice();
            }
        });
        page.addView(save, sizeMargins(-1, dp(58), 0, 0, 0, 12));
        if (!firstLaunch) {
            Button back = secondaryButton(t("back"));
            back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { showModeChoice(); } });
            page.addView(back, sizeMargins(-1, dp(52), 0, 0, 0, 0));
        }
    }

    private void createPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(247, 251, 248));
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(26), dp(22), dp(30));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
    }

    private void showModeChoice() {
        createPage();
        page.addView(text("AI Kitchen Scale", 29, dark(), true));
        page.addView(text(t("choose_mode"), 16, muted(), false), margins(0, 10, 0, 24));
        Button scale = actionButton(t("scale_mode"));
        scale.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { selectedMode = MODE_SCALE; showPhotoGuide(); }
        });
        page.addView(scale, sizeMargins(-1, dp(58), 0, 0, 0, 14));
        page.addView(text(mk("Best accuracy: photograph the food and the scale display together.", "Најдобра точност: фотографирајте ги храната и екранот на вагата заедно."), 14, muted(), false), margins(4, 0, 4, 24));
        Button estimate = secondaryButton(t("estimate_mode"));
        estimate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { selectedMode = MODE_ESTIMATE; showPhotoGuide(); }
        });
        page.addView(estimate, sizeMargins(-1, dp(58), 0, 0, 0, 14));
        page.addView(text(t("manual_weight_notice"), 14, muted(), false));
        Button daily = secondaryButton("📊 " + t("daily"));
        daily.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { showDailySummary(); } });
        page.addView(daily, sizeMargins(-1, dp(52), 0, 18, 0, 0));
        Button goal = secondaryButton("🎯 " + t("goal"));
        goal.setOnClickListener(new View.OnClickListener(){ @Override public void onClick(View v){ showGoal(); }});
        page.addView(goal, sizeMargins(-1, dp(52), 0, 10, 0, 0));
        Button languages = secondaryButton("🌐 " + t("language"));
        languages.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { showLanguageChoice(false); } });
        page.addView(languages, sizeMargins(-1, dp(52), 0, 22, 0, 0));
    }

    private String todayKey() { return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()); }

    private void showGoal(){
        createPage(); page.addView(text(t("goal"),27,dark(),true));
        final String[][] g={{"Lose weight","Maintain weight","Gain weight"},{"Слабеење","Одржување тежина","Зголемување тежина"},{"Humbje peshe","Ruajtje peshe","Shtim peshe"},{"減量","体重維持","増量"},{"Perder peso","Mantener peso","Ganar peso"},{"Abnehmen","Gewicht halten","Zunehmen"},{"Perdre du poids","Maintenir le poids","Prendre du poids"},{"Perdere peso","Mantenere il peso","Aumentare peso"},{"Perder peso","Manter peso","Ganhar peso"}};
        final Spinner s=new Spinner(this); s.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,g[languageIndex()]));
        s.setSelection(getSharedPreferences("settings",MODE_PRIVATE).getInt("goal",1)); page.addView(s,sizeMargins(-1,dp(58),0,20,0,18));
        Button save=actionButton(t("continue")); save.setOnClickListener(new View.OnClickListener(){@Override public void onClick(View v){getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("goal",s.getSelectedItemPosition()).apply();showModeChoice();}}); page.addView(save,sizeMargins(-1,dp(56),0,0,0,0));
    }

    private void saveDaily(int calories, double protein, double fat, double carbs) {
        String key = todayKey();
        android.content.SharedPreferences p = getSharedPreferences("daily_log", MODE_PRIVATE);
        p.edit()
            .putInt(key + "_meals", p.getInt(key + "_meals", 0) + 1)
            .putInt(key + "_cal", p.getInt(key + "_cal", 0) + calories)
            .putFloat(key + "_protein", p.getFloat(key + "_protein", 0) + (float)protein)
            .putFloat(key + "_fat", p.getFloat(key + "_fat", 0) + (float)fat)
            .putFloat(key + "_carbs", p.getFloat(key + "_carbs", 0) + (float)carbs).apply();
    }

    private void showDailySummary() {
        createPage();
        String key = todayKey();
        android.content.SharedPreferences p = getSharedPreferences("daily_log", MODE_PRIVATE);
        String[] labels = nutritionLabels();
        page.addView(text(t("daily"), 27, dark(), true));
        LinearLayout totals = card();
        addRow(totals, t("meals"), String.valueOf(p.getInt(key + "_meals", 0)));
        addRow(totals, labels[0], p.getInt(key + "_cal", 0) + " kcal");
        addRow(totals, labels[1], format1(p.getFloat(key + "_protein", 0)) + " g");
        addRow(totals, labels[2], format1(p.getFloat(key + "_fat", 0)) + " g");
        addRow(totals, labels[3], format1(p.getFloat(key + "_carbs", 0)) + " g");
        page.addView(totals, sizeMargins(-1, -2, 0, 20, 0, 16));
        Button back = actionButton(t("back"));
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { showModeChoice(); } });
        page.addView(back, sizeMargins(-1, dp(56), 0, 8, 0, 0));
    }

    private void showPhotoGuide() {
        createPage();
        page.addView(text(selectedMode == MODE_SCALE ? t("photo_scale") : t("photo_no_scale"), 27, dark(), true));
        page.addView(text(mk("For a reliable result:", "За сигурен резултат:"), 17, dark(), true), margins(0, 20, 0, 10));
        page.addView(text(mk("• Show the full plate\n• Use good light\n• Keep the food clearly visible\n• Photograph from above", "• Прикажете ја целата чинија\n• Користете добро осветлување\n• Храната нека биде јасно видлива\n• Фотографирајте одозгора"), 16, muted(), false));
        if (selectedMode == MODE_SCALE) page.addView(text(mk("• Make sure every digit on the scale display is readable", "• Проверете дали сите бројки на вагата се читливи"), 16, green(), true), margins(0, 8, 0, 0));
        Button camera = actionButton(t("take_photo"));
        camera.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { takePhoto(); }
        });
        page.addView(camera, sizeMargins(-1, dp(58), 0, 28, 0, 12));
        Button choose = secondaryButton(t("gallery"));
        choose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { selectPhoto(); }
        });
        page.addView(choose, sizeMargins(-1, dp(58), 0, 0, 0, 12));
        Button back = secondaryButton(t("back"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { showModeChoice(); }
        });
        page.addView(back, sizeMargins(-1, dp(52), 0, 0, 0, 0));
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO);
    }

    private void takePhoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "ai-kitchen-scale-" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        selectedPhoto = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (selectedPhoto == null) {
            Toast.makeText(this, mk("Could not create a photo", "Фотографијата не можеше да се создаде"), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedPhoto);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(intent, TAKE_PHOTO);
        } catch (Exception error) {
            getContentResolver().delete(selectedPhoto, null, null);
            selectedPhoto = null;
            Toast.makeText(this, mk("Camera is not available", "Камерата не е достапна"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri photo = data.getData();
            if (photo != null) { selectedPhoto = photo; showConfirmation(); }
        } else if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK && selectedPhoto != null) {
                showConfirmation();
            } else if (selectedPhoto != null) {
                getContentResolver().delete(selectedPhoto, null, null);
                selectedPhoto = null;
            }
        }
    }

    private void showConfirmation() {
        createPage();
        page.addView(text(t("confirm_details"), 27, dark(), true));
        ImageView preview = new ImageView(this);
        preview.setImageURI(selectedPhoto);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        page.addView(preview, sizeMargins(-1, dp(210), 0, 16, 0, 18));
        page.addView(text(confirmFoodLabel(), 15, muted(), false));
        foodSpinner = new Spinner(this);
        foodSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, foodChoices()));
        foodSpinner.setSelection(0);
        foodConfirmed = false;
        foodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { foodConfirmed = false; }
            @Override public void onNothingSelected(AdapterView<?> parent) { foodConfirmed = false; }
        });
        page.addView(foodSpinner, sizeMargins(-1, dp(56), 0, 6, 0, 6));
        recognitionStatus = text(photoText(0), 14, muted(), false);
        page.addView(recognitionStatus, margins(0, 0, 0, 8));
        Button confirmFood = secondaryButton(t("confirm_food"));
        confirmFood.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (foodSpinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(MainActivity.this, mk("Select a food first", "Прво изберете храна"), Toast.LENGTH_SHORT).show();
                    return;
                }
                foodConfirmed = true;
                recognitionStatus.setText(aiLabels()[0] + ": " + foodSpinner.getSelectedItem().toString());
                recognitionStatus.setTextColor(green());
            }
        });
        page.addView(confirmFood, sizeMargins(-1, dp(52), 0, 0, 0, 18));
        page.addView(text(selectedMode == MODE_SCALE ? photoText(1) : t("manual_weight_notice"), 15, muted(), false));
        LinearLayout weightRow = new LinearLayout(this);
        weightRow.setOrientation(LinearLayout.HORIZONTAL);
        weightInput = numberInput(t("weight"));
        // No sample weight: the user must enter a measured or manually estimated portion.
        weightRow.addView(weightInput, new LinearLayout.LayoutParams(0, dp(58), 1));
        weightUnitSpinner = new Spinner(this);
        String[] weightUnits = {"g", "oz", "lb"};
        weightUnitSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, weightUnits));
        weightRow.addView(weightUnitSpinner, new LinearLayout.LayoutParams(dp(92), dp(58)));
        page.addView(weightRow, sizeMargins(-1, dp(58), 0, 8, 0, 18));
        page.addView(text(t("preparation"), 15, muted(), false));
        cookingSpinner = new Spinner(this);
        String[] methods = cookingMethods();
        cookingSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, methods));
        page.addView(cookingSpinner, sizeMargins(-1, dp(56), 0, 6, 0, 18));
        page.addView(text(t("oil"), 15, muted(), false));
        LinearLayout oilRow = new LinearLayout(this);
        oilRow.setOrientation(LinearLayout.HORIZONTAL);
        oilInput = numberInput("0");
        oilInput.setText("0");
        oilRow.addView(oilInput, new LinearLayout.LayoutParams(0, dp(58), 1));
        oilUnitSpinner = new Spinner(this);
        String[] oilUnits = {"g", "oz", "lb"};
        oilUnitSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, oilUnits));
        oilRow.addView(oilUnitSpinner, new LinearLayout.LayoutParams(dp(92), dp(58)));
        page.addView(oilRow, sizeMargins(-1, dp(58), 0, 6, 0, 24));
        Button calculate = actionButton(t("calculate"));
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { calculateResult(); }
        });
        page.addView(calculate, sizeMargins(-1, dp(58), 0, 0, 0, 12));
        page.addView(text(aiConfirmationNote(), 13, muted(), false));
        analyzeFoodWithServer(selectedPhoto);
    }

    private void calculateResult() {
        if (foodSpinner.getSelectedItemPosition() == 0 || !foodConfirmed) {
            Toast.makeText(this, mk("Confirm the selected food first", "Прво потврдете ја избраната храна"), Toast.LENGTH_SHORT).show();
            return;
        }
        double enteredWeight = parse(weightInput.getText().toString());
        String weightUnit = weightUnitSpinner.getSelectedItem().toString();
        double grams = toGrams(enteredWeight, weightUnit);
        double enteredOil = parse(oilInput.getText().toString());
        double oil = toGrams(enteredOil, oilUnitSpinner.getSelectedItem().toString());
        if (grams <= 0 || grams > 5000) {
            Toast.makeText(this, mk("Enter a valid weight", "Внесете важечка тежина"), Toast.LENGTH_SHORT).show();
            return;
        }
        int methodPosition = cookingSpinner.getSelectedItemPosition();
        String method = cookingSpinner.getSelectedItem().toString();
        int foodIndex = foodSpinner.getSelectedItemPosition() - 1;
        double[] food = nutrients[foodIndex];
        double factor = 1.0;
        if (methodPosition == 1) factor = 1.06;
        if (methodPosition == 2) factor = 1.18;
        if (methodPosition == 3) factor = 0.97;
        if (methodPosition == 4) factor = 0.90;
        double scale = grams / 100.0;
        int calories = (int)Math.round(food[0] * scale * factor + oil * 9);
        showResult(foodIndex, grams, enteredWeight, weightUnit, method, calories, food[1] * scale, food[2] * scale * factor + oil, food[3] * scale);
    }

    private void analyzeFoodWithServer(final Uri photo) {
        if (ServerConfig.ANALYSIS_URL.length() == 0) {
            recognitionStatus.setText(photoText(3));
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                HttpURLConnection connection = null;
                try {
                    byte[] imageBytes = readPhoto(photo);
                    if (imageBytes.length > 6 * 1024 * 1024) throw new Exception("Image is larger than 6 MB");
                    String mime = "image/jpeg";
                    JSONObject request = new JSONObject();
                    request.put("imageBase64", Base64.encodeToString(imageBytes, Base64.NO_WRAP));
                    request.put("mimeType", mime);
                    request.put("licenseId", ServerConfig.LICENSE_ID);
                    if (!ServerConfig.ANALYSIS_URL.startsWith("https://")) throw new Exception("HTTPS_REQUIRED");
                    connection = (HttpURLConnection)new URL(ServerConfig.ANALYSIS_URL).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(45000);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
                    connection.setFixedLengthStreamingMode(body.length);
                    OutputStream output = connection.getOutputStream();
                    output.write(body);
                    output.close();
                    int code = connection.getResponseCode();
                    InputStream input = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
                    String responseText = new String(readAll(input), StandardCharsets.UTF_8);
                    if (code < 200 || code >= 300) {
                        JSONObject serverError = new JSONObject(responseText);
                        throw new Exception(serverError.optString("error", "SERVER_ERROR"));
                    }
                    final JSONObject result = new JSONObject(responseText);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (photo.equals(selectedPhoto)) applyRecognition(result);
                        }
                    });
                } catch (final Exception error) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!photo.equals(selectedPhoto)) return;
                            String code = error.getMessage() == null ? "" : error.getMessage();
                            if (code.equals("AI_BUDGET_LIMIT") || code.equals("INVALID_LICENSE") || code.equals("RATE_LIMIT")) recognitionStatus.setText(photoText(3));
                            else if (code.equals("SERVER_NOT_CONFIGURED")) recognitionStatus.setText(photoText(3));
                            else recognitionStatus.setText(mk("Could not recognize this photo — select the food manually.", "Фотографијата не беше препознаена — изберете ја храната рачно."));
                        }
                    });
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        }).start();
    }

    private void applyRecognition(JSONObject result) {
        String foodName = result.optString("foodName", "");
        double confidence = result.optDouble("confidence", 0);
        int remaining = result.optInt("remainingScans", -1);
        String[] ai = aiLabels();
        String remainingText = remaining >= 0 ? " · " + remaining + " " + ai[5] : "";
        int match = 0;
        for (int i = 0; i < foods.length; i++) {
            if (foods[i].equalsIgnoreCase(foodName)) { match = i + 1; break; }
        }
        foodSpinner.setSelection(match);
        foodConfirmed = false;
        if (match == 0 || !result.optBoolean("recognized", false)) {
            recognitionStatus.setText(ai[1] + remainingText);
        } else {
            recognitionStatus.setText(ai[2] + ": " + foodChoices()[match] + " (" + Math.round(confidence * 100) + "% " + ai[3] + "). " + ai[4] + remainingText);
        }
    }

    private byte[] readPhoto(Uri photo) throws Exception {
        InputStream input = getContentResolver().openInputStream(photo);
        if (input == null) throw new Exception("Cannot open image");
        Bitmap original;
        try { original = BitmapFactory.decodeStream(input); }
        finally { input.close(); }
        if (original == null) throw new Exception("Cannot decode image");
        int width = original.getWidth();
        int height = original.getHeight();
        int maxSide = Math.max(width, height);
        Bitmap upload = original;
        if (maxSide > 1280) {
            double ratio = 1280.0 / maxSide;
            upload = Bitmap.createScaledBitmap(original, Math.max(1, (int)(width * ratio)), Math.max(1, (int)(height * ratio)), true);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        upload.compress(Bitmap.CompressFormat.JPEG, 82, output);
        if (upload != original) upload.recycle();
        original.recycle();
        return output.toByteArray();
    }

    private byte[] readAll(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        return output.toByteArray();
    }

    private void showResult(int foodIndex, double grams, double enteredWeight, String weightUnit, String method, int calories, double protein, double fat, double carbs) {
        double[] food = nutrients[foodIndex];
        double scale = grams / 100.0;
        createPage();
        page.addView(text(t("result"), 27, dark(), true));
        page.addView(text(foodChoices()[foodIndex + 1] + " · " + method, 18, dark(), true), margins(0, 18, 0, 6));
        page.addView(text(formatWeight(enteredWeight, weightUnit) + " " + weightUnit, 40, green(), true));
        String[] labels = nutritionLabels();
        if (!weightUnit.equals("g")) page.addView(text(labels[11] + " " + format0(grams) + " g", 14, muted(), false));
        String certainty = t(selectedMode == MODE_SCALE ? "weight_source_scale" : "weight_source_manual");
        page.addView(text(certainty, 15, muted(), false), margins(0, 2, 0, 18));
        LinearLayout card = card();
        addRow(card, labels[0], calories + " kcal");
        addRow(card, labels[1], format1(protein) + " g");
        addRow(card, labels[2], format1(fat) + " g");
        addRow(card, labels[3], format1(carbs) + " g");
        addRow(card, labels[4], format1(food[4] * scale) + " g");
        addRow(card, labels[5], format1(food[5] * scale) + " g");
        page.addView(card, sizeMargins(-1, -2, 0, 0, 0, 16));
        page.addView(text(labels[6], 20, dark(), true), margins(0, 2, 0, 10));
        LinearLayout micro = card();
        addRow(micro, labels[7], format0(food[6] * scale) + " mg");
        addRow(micro, labels[8], format0(food[7] * scale) + " mg");
        addRow(micro, labels[9], format1(food[8] * scale) + " mg");
        addRow(micro, labels[10], format1(food[9] * scale) + " mg");
        addRow(micro, "Vitamin B6", format1(food[10] * scale) + " mg");
        addRow(micro, "Vitamin B12", format1(food[11] * scale) + " µg");
        page.addView(micro, sizeMargins(-1, -2, 0, 0, 0, 16));
        page.addView(text(nutritionNote(), 13, muted(), false));
        final Button save = secondaryButton("💾 " + t("save_meal"));
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                saveDaily(calories, protein, fat, carbs);
                save.setText("✓ " + t("saved"));
                save.setEnabled(false);
            }
        });
        page.addView(save, sizeMargins(-1, dp(56), 0, 18, 0, 0));
        Button again = actionButton(t("again"));
        again.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { showModeChoice(); }
        });
        page.addView(again, sizeMargins(-1, dp(56), 0, 22, 0, 0));
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(6));
        card.setBackgroundColor(Color.WHITE);
        return card;
    }

    private void addRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(text(label, 16, muted(), false), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text(value, 16, dark(), true));
        parent.addView(row, sizeMargins(-1, -2, 0, 0, 0, 12));
    }

    private EditText numberInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(18);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return input;
    }

    private Button actionButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackgroundColor(green());
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(16);
        button.setTextColor(green());
        button.setAllCaps(false);
        button.setBackgroundColor(Color.WHITE);
        return button;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private double parse(String value) {
        try { return Double.parseDouble(value.replace(',', '.')); }
        catch (Exception ignored) { return 0; }
    }
    private double toGrams(double value, String unit) {
        if (unit.equals("oz")) return value * 28.349523125;
        if (unit.equals("lb")) return value * 453.59237;
        return value;
    }
    private double fromGrams(double grams, String unit) {
        if (unit.equals("oz")) return grams / 28.349523125;
        if (unit.equals("lb")) return grams / 453.59237;
        return grams;
    }
    private String formatWeight(double value, String unit) {
        return unit.equals("g") ? format0(value) : String.format(Locale.US, "%.2f", value);
    }
    private String format0(double value) { return String.format(Locale.US, "%.0f", value); }
    private String format1(double value) { return String.format(Locale.US, "%.1f", value); }
    private int dark() { return Color.rgb(25, 35, 30); }
    private int muted() { return Color.rgb(82, 97, 90); }
    private int green() { return Color.rgb(40, 122, 85); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) { return sizeMargins(-1, -2, left, top, right, bottom); }
    private LinearLayout.LayoutParams sizeMargins(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return p;
    }
}
