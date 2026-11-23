package net.kdt.pojavlaunch.cleave.theme

import android.app.Activity
import android.content.Context
import androidx.annotation.FontRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import net.kdt.pojavlaunch.R

/** Bundled, offline font choices. IDs are stable because they are saved in preferences. */
data class CleaveFontChoice(val id: String, val label: String, @FontRes val resource: Int = 0) {
    fun family(): FontFamily = if (resource == 0) FontFamily.SansSerif else FontFamily(Font(resource))
}

object CleaveFonts {
    private const val PREFS = "cleave_font_style"
    private const val KEY = "selected"
    val choices = listOf(
        CleaveFontChoice("default", "Default"),
        CleaveFontChoice("minecraft", "Minecraft", R.font.minecraft_ten),
        CleaveFontChoice("pressstart2p", "Pixel · Press Start 2P", R.font.font_pressstart2p),
        CleaveFontChoice("silkscreen", "Pixel · Silkscreen", R.font.font_silkscreen),
        CleaveFontChoice("vt323", "Pixel · VT323", R.font.font_vt323),
        CleaveFontChoice("lato", "Lato", R.font.font_lato),
        CleaveFontChoice("poppins", "Poppins", R.font.font_poppins),
        CleaveFontChoice("firasans", "Fira Sans", R.font.font_firasans),
        CleaveFontChoice("kanit", "Kanit", R.font.font_kanit),
        CleaveFontChoice("oxygen", "Oxygen", R.font.font_oxygen),
        CleaveFontChoice("ibmplexmono", "IBM Plex Mono", R.font.font_ibmplexmono),
        CleaveFontChoice("varelaround", "Varela Round", R.font.font_varelaround),
        CleaveFontChoice("titilliumweb", "Titillium Web", R.font.font_titilliumweb),
        CleaveFontChoice("bebasneue", "Bebas Neue", R.font.font_bebasneue),
        CleaveFontChoice("anton", "Anton", R.font.font_anton),
        CleaveFontChoice("arvo", "Arvo", R.font.font_arvo),
        CleaveFontChoice("comforter", "Comforter", R.font.font_comforter),
        CleaveFontChoice("amaticsc", "Amatic SC", R.font.font_amaticsc),
        CleaveFontChoice("abrilfatface", "Abril Fatface", R.font.font_abrilfatface),
        CleaveFontChoice("lobster", "Lobster", R.font.font_lobster),
        CleaveFontChoice("righteous", "Righteous", R.font.font_righteous),
        CleaveFontChoice("alegreyasans", "Alegreya Sans", R.font.font_alegreyasans),
        CleaveFontChoice("hind", "Hind", R.font.font_hind),
        CleaveFontChoice("alfaslabone", "Alfa Slab One", R.font.font_alfaslabone),
        CleaveFontChoice("acme", "Acme", R.font.font_acme),
        CleaveFontChoice("architectsdaughter", "Architects Daughter", R.font.font_architectsdaughter),
        CleaveFontChoice("barlow", "Barlow", R.font.font_barlow),
        CleaveFontChoice("breeserif", "Bree Serif", R.font.font_breeserif),
        CleaveFontChoice("creepster", "Creepster", R.font.font_creepster),
        CleaveFontChoice("mukta", "Mukta", R.font.font_mukta),
        CleaveFontChoice("patrickhand", "Patrick Hand", R.font.font_patrickhand),
        CleaveFontChoice("chakrapetch", "Chakra Petch", R.font.font_chakrapetch),
        CleaveFontChoice("asapcondensed", "Asap Condensed", R.font.font_asapcondensed),
        CleaveFontChoice("inconsolata", "Inconsolata", R.font.font_inconsolata),
        CleaveFontChoice("cabincondensed", "Cabin Condensed", R.font.font_cabincondensed)
    )
    private var initialized = false
    var selectedId by mutableStateOf("default")
        private set

    fun initialize(context: Context) {
        if (initialized) return
        val saved = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "default")
        selectedId = choices.firstOrNull { it.id == saved }?.id ?: "default"
        initialized = true
    }

    fun select(context: Context, choice: CleaveFontChoice) {
        require(choice in choices)
        initialize(context)
        selectedId = choice.id
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, choice.id).apply()
        (context as? Activity)?.let { applyNativeTheme(it) }
    }

    /** Apply the selected family when a legacy Android activity or dialog is inflated. */
    @JvmStatic
    fun applyNativeTheme(activity: Activity) {
        initialize(activity)
        val style = when (selectedId) {
            "default" -> R.style.CleaveFont_default
            "minecraft" -> R.style.CleaveFont_minecraft
            "pressstart2p" -> R.style.CleaveFont_pressstart2p
            "silkscreen" -> R.style.CleaveFont_silkscreen
            "vt323" -> R.style.CleaveFont_vt323
            "lato" -> R.style.CleaveFont_lato
            "poppins" -> R.style.CleaveFont_poppins
            "firasans" -> R.style.CleaveFont_firasans
            "kanit" -> R.style.CleaveFont_kanit
            "oxygen" -> R.style.CleaveFont_oxygen
            "ibmplexmono" -> R.style.CleaveFont_ibmplexmono
            "varelaround" -> R.style.CleaveFont_varelaround
            "titilliumweb" -> R.style.CleaveFont_titilliumweb
            "bebasneue" -> R.style.CleaveFont_bebasneue
            "anton" -> R.style.CleaveFont_anton
            "arvo" -> R.style.CleaveFont_arvo
            "comforter" -> R.style.CleaveFont_comforter
            "amaticsc" -> R.style.CleaveFont_amaticsc
            "abrilfatface" -> R.style.CleaveFont_abrilfatface
            "lobster" -> R.style.CleaveFont_lobster
            "righteous" -> R.style.CleaveFont_righteous
            "alegreyasans" -> R.style.CleaveFont_alegreyasans
            "hind" -> R.style.CleaveFont_hind
            "alfaslabone" -> R.style.CleaveFont_alfaslabone
            "acme" -> R.style.CleaveFont_acme
            "architectsdaughter" -> R.style.CleaveFont_architectsdaughter
            "barlow" -> R.style.CleaveFont_barlow
            "breeserif" -> R.style.CleaveFont_breeserif
            "creepster" -> R.style.CleaveFont_creepster
            "mukta" -> R.style.CleaveFont_mukta
            "patrickhand" -> R.style.CleaveFont_patrickhand
            "chakrapetch" -> R.style.CleaveFont_chakrapetch
            "asapcondensed" -> R.style.CleaveFont_asapcondensed
            "inconsolata" -> R.style.CleaveFont_inconsolata
            "cabincondensed" -> R.style.CleaveFont_cabincondensed
            else -> R.style.CleaveFont_default
        }
        activity.theme.applyStyle(style, true)
    }

    fun selected(): CleaveFontChoice = choices.firstOrNull { it.id == selectedId } ?: choices.first()
}
