# VoirDrama Extension for Aniyomi

<p align="center">
  <img src="res/mipmap-xxxhdpi/ic_launcher.png" alt="VoirDrama Logo" width="120">
</p>

<p align="center">
  <strong>French drama streaming extension for Aniyomi</strong>
</p>

---

## 📖 About

This is an **Aniyomi extension** that provides access to [VoirDrama](https://voirdrama.to), a popular French drama streaming website. Watch your favorite Asian dramas (Korean, Chinese, Japanese, Thai, and more) in VOSTFR (French subtitles) directly within the Aniyomi app.

### Features

- ✅ **Browse & Search** - Discover thousands of drama titles
- ✅ **Advanced Filters** - Filter by genre, year, status, format, and language
- ✅ **Multiple Video Servers** - Support for 5 different video players (VidMoly, Filemoon, Voe, StreamTape, VK)
- ✅ **Deep Link Support** - Open drama directly from browser links
- ✅ **Customizable Thumbnails** - Adjust image quality to save bandwidth
- ✅ **Preferred Player & Quality** - Set your favorite video player and quality for automatic sorting
- ✅ **Latest Updates** - Stay up to date with new episodes

---

## 🚀 Installation

### Prerequisites

- **[Aniyomi](https://github.com/aniyomiorg/aniyomi)** app installed on your Android device
- Android 6.0 (API 23) or higher

### Installation Steps

1. Download the latest `.apk` file from the [Releases](../../releases) page
2. Open **Aniyomi** app
3. Go to **Browse** → **Extensions**
4. Tap the **Install** button (📦 icon) in the top right
5. Select the downloaded `.apk` file
6. Grant necessary permissions if prompted
7. The extension will appear in your **Sources** list

---

## 🎯 Usage

### Browsing Drama

1. Open Aniyomi and go to **Browse** → **Sources**
2. Select **VoirDrama** (marked with 🇫🇷 Français)
3. Browse **Latest** updates or **Popular** dramas
4. Use the **Filter** button to refine your search

### Search Filters

The extension provides comprehensive search filters:

- **Sort By**: Relevance, Popularity, Latest, Alphabetical, Rating, Views, New
- **Format**: TV, Movie, TV Short, OVA, ONA, Special
- **Language**: VF (French dub), VOSTFR (French subtitles)
- **Year**: Filter by release year
- **Status**: Completed, Ongoing, Canceled, On Hold
- **Genres**: Action, Aventure, Comédie, Romance, Thriller, Historique, Médical, and more

### Deep Link Support

You can open drama directly from your browser:
1. Visit any drama page on voirdrama.to
2. The link will automatically open in Aniyomi
3. The drama will be loaded directly in the app

---

## ⚙️ Configuration

Access settings via **Extension Settings** in Aniyomi.

### Preferred Player

Choose your favorite video player. Videos from this player will appear first in the list:

- **myTV** (VidMoly) - Default
- **MOON** (Filemoon)
- **VOE**
- **Stape** (StreamTape)
- **FHD1** (VK)

### Preferred Quality

Select your preferred video quality. This quality will be prioritized when available:

- **1080p** - Default
- **720p**
- **480p**
- **360p**

Videos are sorted by player preference first, then by quality preference.

### Thumbnail Quality

Customize image quality to balance visual experience and data usage:

- **110x150** - Very Small (minimal data usage)
- **125x180** - Small
- **175x238** - Medium Low
- **193x278** - Default
- **350x476** - Medium High
- **460x630** - Large
- **Original** - Highest quality (highest data usage)

---

## 🎬 Supported Video Players

The extension automatically extracts videos from multiple sources:

| Player | Host | Quality |
|--------|------|---------|
| **LECTEUR VIDM** | VidMoly | Multiple qualities |
| **LECTEUR MOON** | Filemoon (f16px) | Multiple qualities |
| **LECTEUR VOE** | Voe.sx | Multiple qualities |
| **LECTEUR Stape** | StreamTape | Single quality |
| **LECTEUR FHD1** | VK/Mail.ru | Multiple qualities |

The app will display all available video sources for each episode. Choose the one that works best for you.

---

## 🛠️ Technical Details

### Built With

- **Language**: Kotlin
- **Min SDK**: Android 6.0 (API 23)
- **Framework**: Aniyomi Extension API
- **Architecture**: ParsedAnimeHttpSource

### Dependencies

This extension uses external video extractor libraries:
- `vidmoly-extractor` - VidMoly player support
- `filemoon-extractor` - Filemoon/f16px player support
- `voe-extractor` - Voe player support
- `streamtape-extractor` - StreamTape player support
- `vk-extractor` - VK/Mail.ru player support

---

## 🐛 Troubleshooting

### No videos appear
- Try a different video server/player
- Check your internet connection
- Some servers may be temporarily down

### Extension not showing in sources
- Make sure you installed the correct `.apk` file
- Restart the Aniyomi app
- Check that the extension is enabled in settings

### Videos won't play
- Some video hosts may be blocked in your region
- Try using a VPN if necessary
- Switch to a different video server

---

## 📝 License

This extension is provided as-is for personal use. Please respect the original content creators and copyright holders.

---

## ⚠️ Disclaimer

This extension is not affiliated with or endorsed by VoirDrama or Aniyomi. It is a third-party extension created for educational purposes. Users are responsible for complying with their local laws regarding streaming content.

---

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest new features
- Submit pull requests
- Improve documentation

---

<p align="center">
  Made with ❤️ for the drama community
</p>
