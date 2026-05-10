package com.virtualworld.easymusic.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.virtualworld.easymusic.R
import com.virtualworld.easymusic.ui.theme.DarkBackground
import com.virtualworld.easymusic.ui.theme.DarkCard
import com.virtualworld.easymusic.ui.theme.DarkSurface
import com.virtualworld.easymusic.ui.theme.Teal400
import com.virtualworld.easymusic.ui.theme.TextGray
import com.virtualworld.easymusic.ui.theme.TextWhite

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)

private val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("", "System", "System default"),
    LanguageOption("en", "English", "English"),
    LanguageOption("es", "Español", "Spanish"),
    LanguageOption("ar", "العربية", "Arabic"),
    LanguageOption("bn", "বাংলা", "Bengali"),
    LanguageOption("de", "Deutsch", "German"),
    LanguageOption("fr", "Français", "French"),
    LanguageOption("hi", "हिन्दी", "Hindi"),
    LanguageOption("in", "Bahasa Indonesia", "Indonesian"),
    LanguageOption("it", "Italiano", "Italian"),
    LanguageOption("ja", "日本語", "Japanese"),
    LanguageOption("ko", "한국어", "Korean"),
    LanguageOption("nl", "Nederlands", "Dutch"),
    LanguageOption("pt", "Português", "Portuguese"),
    LanguageOption("ru", "Русский", "Russian"),
    LanguageOption("th", "ไทย", "Thai"),
    LanguageOption("tr", "Türkçe", "Turkish"),
    LanguageOption("uk", "Українська", "Ukrainian"),
    LanguageOption("vi", "Tiếng Việt", "Vietnamese"),
    LanguageOption("zh", "中文", "Chinese")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val currentLocale = remember {
        AppCompatDelegate.getApplicationLocales().toLanguageTags()
    }
    var selectedCode by remember {
        mutableStateOf(
            if (currentLocale.isBlank()) "" else currentLocale.split(",").first().split("-").first()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
                    color = TextWhite,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = TextWhite
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Teal400,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                }
            }

            items(SUPPORTED_LANGUAGES, key = { it.code }) { language ->
                val isSelected = language.code == selectedCode
                LanguageItem(
                    language = language,
                    isSelected = isSelected,
                    isSystemDefault = language.code.isEmpty(),
                    systemDefaultLabel = stringResource(R.string.system_default),
                    onClick = {
                        selectedCode = language.code
                        val locales = if (language.code.isEmpty()) {
                            LocaleListCompat.getEmptyLocaleList()
                        } else {
                            LocaleListCompat.forLanguageTags(language.code)
                        }
                        AppCompatDelegate.setApplicationLocales(locales)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LanguageItem(
    language: LanguageOption,
    isSelected: Boolean,
    isSystemDefault: Boolean,
    systemDefaultLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Teal400.copy(alpha = 0.15f) else DarkCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isSystemDefault) systemDefaultLabel else language.nativeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Teal400 else TextWhite
            )
            if (!isSystemDefault) {
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Teal400,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
