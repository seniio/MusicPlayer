package com.muse.app.data.repository

import com.muse.app.data.model.OnlineTrack

data class CuratedPlaylist(
    val id: String,
    val title: String,
    val subtitle: String,
    val trackCountText: String,
    val badge: String,
    val tracks: List<OnlineTrack>
)

object CuratedPlaylistsRepository {

    private fun track(title: String, artist: String, album: String = ""): OnlineTrack {
        return OnlineTrack(
            videoId = "search:$title $artist",
            title = title,
            artist = artist,
            album = album,
            thumbnailUrl = ""
        )
    }

    // --- 1. Top 50 Indonesia (2026 Chart) ---
    val TOP_50_INDONESIA = CuratedPlaylist(
        id = "top_50_indo",
        title = "Top 50 Indonesia (2026)",
        subtitle = "50 Lagu Terpopuler & Paling Banyak Didengarkan di Indonesia 2026",
        trackCountText = "50 Lagu",
        badge = "INDONESIA 2026",
        tracks = listOf(
            track("Teh Hijau", "Tulus"),
            track("Jatuh Suka", "Tulus"),
            track("Adu Pandang", "Rizky Febian"),
            track("Ada Titik-Titik di Ujung Doa", "Sal Priadi"),
            track("Kita Usahakan Rumah Itu", "Sal Priadi"),
            track("Kota Ini Tak Sama Tanpamu", "Nadhif Basalamah"),
            track("Bergema Sampai Selamanya", "Nadhif Basalamah"),
            track("Percuma", "Mahalini"),
            track("Dunia Yang Nanti", "Raim Laode"),
            track("Bersenja Gurau", "Raim Laode"),
            track("Iqro'", "Raim Laode"),
            track("Lesung Pipi", "Raim Laode"),
            track("Ini Abadi", "Perunggu"),
            track("33x", "Perunggu"),
            track("Everything U Are", "Hindia"),
            track("Cincin", "Hindia"),
            track("Maha Benar", "Salma Salsabil"),
            track("Jangan Paksa Rindu", "Beda, Ifan Seventeen"),
            track("Sesi Potret", "eńau, Ari Lesmana"),
            track("Tarot", ".Feast"),
            track("Nina", ".Feast"),
            track("Sedia Aku Sebelum Hujan", "Idgitaf"),
            track("Berapa Kali Kita Akan Saling Memaafkan", "Pamungkas"),
            track("Monolog", "Pamungkas"),
            track("Serana", "For Revenge"),
            track("Amnesia", "Al Ghazali"),
            track("rollerblade", "no na"),
            track("Garam dan Madu (Sakit Dadaku)", "Tenxi, Naykilla, Jemsii"),
            track("Tabola Bale", "Silet Open Up"),
            track("Kisinan 2", "Masdddho"),
            track("Samar", "Masdddho"),
            track("Sigar", "Denny Caknan"),
            track("Cundamani", "Denny Caknan"),
            track("Nemen", "Gildcoustic"),
            track("Nanti Kita Seperti Ini", "Batas Senja"),
            track("Semua Aku Dirayakan", "Nadin Amizah"),
            track("Rayuan Perempuan Gila", "Nadin Amizah"),
            track("Kupu-Kupu", "Tiara Andini"),
            track("Pesan Terakhir", "Lyodra"),
            track("Sialan", "Juicy Luicy, Adrian Khalif"),
            track("Lampu Kuning", "Juicy Luicy"),
            track("Kita Bikin Romantis", "MALIQ & D'Essentials"),
            track("Tak Segampang Itu", "Anggi Marito"),
            track("Rumah Singgah", "Fabio Asher"),
            track("Tak Ingin Usai", "Keisya Levronka"),
            track("Mangu", "Fourtwnty"),
            track("Boleh Juga", "Salma Salsabil"),
            track("Mati-Matian", "Mahalini"),
            track("Untungnya, Hidup Harus Tetap Berjalan", "Bernadya"),
            track("Satu Bulan", "Bernadya")
        )
    )

    // --- 2. Top 50 Global (2026 Chart) ---
    val TOP_50_GLOBAL = CuratedPlaylist(
        id = "top_50_global",
        title = "Top 50 Global (2026)",
        subtitle = "50 Lagu Terbesar & Paling Banyak Diputar di Seluruh Dunia 2026",
        trackCountText = "50 Lagu",
        badge = "GLOBAL 2026",
        tracks = listOf(
            track("Choosin' Texas", "Ella Langley"),
            track("Housewife", "Tate McRae"),
            track("Boston", "Stella Lefty"),
            track("Been By Now", "Morgan Wallen"),
            track("Dracula", "Tame Impala, JENNIE"),
            track("Nicole Kidman", "ADELA"),
            track("Expectations", "Olivia Rodrigo, Chappell Roan"),
            track("Patient Zero", "Taylor Swift"),
            track("BbY WOW", "Karol G, Judeline, rusowsky"),
            track("Hate That I Made You Love Me", "Ariana Grande"),
            track("I Just Might", "Bruno Mars"),
            track("Risk It All", "Bruno Mars"),
            track("Opalite", "Taylor Swift"),
            track("The Fate of Ophelia", "Taylor Swift"),
            track("DTMF", "Bad Bunny"),
            track("Baile Inolvidable", "Bad Bunny"),
            track("The Great Divide", "Noah Kahan"),
            track("Orbiter", "Noah Kahan"),
            track("Aperture", "Harry Styles"),
            track("Janice STFU", "Drake"),
            track("Dancing in the Flames", "The Weeknd"),
            track("Timeless", "The Weeknd, Playboi Carti"),
            track("The Emptiness Machine", "Linkin Park"),
            track("Heavy Is the Crown", "Linkin Park"),
            track("Die With A Smile", "Lady Gaga, Bruno Mars"),
            track("APT.", "ROSÉ, Bruno Mars"),
            track("BIRDS OF A FEATHER", "Billie Eilish"),
            track("CHIHIRO", "Billie Eilish"),
            track("Taste", "Sabrina Carpenter"),
            track("Bed Chem", "Sabrina Carpenter"),
            track("Good Luck, Babe!", "Chappell Roan"),
            track("Beautiful Things", "Benson Boone"),
            track("Lose Control", "Teddy Swims"),
            track("Too Sweet", "Hozier"),
            track("I Had Some Help", "Post Malone, Morgan Wallen"),
            track("Not Like Us", "Kendrick Lamar"),
            track("Fortnight", "Taylor Swift, Post Malone"),
            track("FE!N", "Travis Scott"),
            track("Houdini", "Eminem"),
            track("Starboy", "The Weeknd"),
            track("As It Was", "Harry Styles"),
            track("Houdini", "Dua Lipa"),
            track("vampire", "Olivia Rodrigo"),
            track("Snooze", "SZA"),
            track("greedy", "Tate McRae"),
            track("MILLION DOLLAR BABY", "Tommy Richman"),
            track("A Bar Song (Tipsy)", "Shaboozey"),
            track("Who", "Jimin"),
            track("Chk Chk Boom", "Stray Kids"),
            track("Stick Season", "Noah Kahan")
        )
    )

    // --- 3. New Releases: Rilis Terbaru 2026 & Terhangat ---
    val NEW_RELEASES_PLAYLIST = CuratedPlaylist(
        id = "new_releases",
        title = "Rilis Terbaru (New Releases 2026)",
        subtitle = "Tembang Paling Baru, Segar, dan Tangga Lagu Teratas 2026",
        trackCountText = "32 Lagu",
        badge = "RILIS 2026",
        tracks = listOf(
            // Indonesia 2026 Hits
            track("Teh Hijau", "Tulus"),
            track("Jatuh Suka", "Tulus"),
            track("Sesi Potret", "eńau, Ari Lesmana"),
            track("Ada Titik-Titik di Ujung Doa", "Sal Priadi"),
            track("Dunia Yang Nanti", "Raim Laode"),
            track("Bersenja Gurau", "Raim Laode"),
            track("Jangan Paksa Rindu", "Beda, Ifan Seventeen"),
            track("Ini Abadi", "Perunggu"),
            track("33x", "Perunggu"),
            track("Everything U Are", "Hindia"),
            track("rollerblade", "no na"),
            track("Tabola Bale", "Silet Open Up"),
            track("Garam dan Madu (Sakit Dadaku)", "Tenxi, Naykilla, Jemsii"),
            track("Sigar", "Denny Caknan"),
            track("Cundamani", "Denny Caknan"),
            track("Kisinan 2", "Masdddho"),
            // Global 2026 Hits
            track("I Just Might", "Bruno Mars"),
            track("Risk It All", "Bruno Mars"),
            track("Opalite", "Taylor Swift"),
            track("The Fate of Ophelia", "Taylor Swift"),
            track("Dracula", "Tame Impala, JENNIE"),
            track("Housewife", "Tate McRae"),
            track("Choosin' Texas", "Ella Langley"),
            track("DTMF", "Bad Bunny"),
            track("Baile Inolvidable", "Bad Bunny"),
            track("Been By Now", "Morgan Wallen"),
            track("APT.", "ROSÉ, Bruno Mars"),
            track("Die With A Smile", "Lady Gaga, Bruno Mars"),
            track("Dancing in the Flames", "The Weeknd"),
            track("Timeless", "The Weeknd, Playboi Carti"),
            track("The Emptiness Machine", "Linkin Park"),
            track("Heavy Is the Crown", "Linkin Park")
        )
    )

    // --- 4. Daily Mix 1: Kombinasi Pop & Hits Pilihan ---
    val DAILY_MIX = CuratedPlaylist(
        id = "daily_mix_1",
        title = "Daily Mix 1",
        subtitle = "Kombinasi lagu pop, indie, dan hits harian pilihan untukmu",
        trackCountText = "20 Lagu",
        badge = "DAILY MIX",
        tracks = listOf(
            track("Gala Bunga Matahari", "Sal Priadi"),
            track("Die With A Smile", "Lady Gaga, Bruno Mars"),
            track("Untungnya, Hidup Harus Tetap Berjalan", "Bernadya"),
            track("BIRDS OF A FEATHER", "Billie Eilish"),
            track("Kita Bikin Romantis", "MALIQ & D'Essentials"),
            track("APT.", "ROSÉ, Bruno Mars"),
            track("Sialan", "Juicy Luicy, Adrian Khalif"),
            track("Taste", "Sabrina Carpenter"),
            track("Rayuan Perempuan Gila", "Nadin Amizah"),
            track("Timeless", "The Weeknd, Playboi Carti"),
            track("Rumah Ke Rumah", "Hindia"),
            track("Good Luck, Babe!", "Chappell Roan"),
            track("Kupu-Kupu", "Tiara Andini"),
            track("Starboy", "The Weeknd"),
            track("Tabola Bale", "Silet Open Up"),
            track("Dan", "Sheila On 7"),
            track("As It Was", "Harry Styles"),
            track("Monokrom", "Tulus"),
            track("Fix You", "Coldplay"),
            track("Garam dan Madu (Sakit Dadaku)", "Tenxi, Naykilla, Jemsii")
        )
    )

    // --- 5. Akustik Santai: Tembang Syahdu Penenang Pikiran ---
    val AKUSTIK_SANTAI = CuratedPlaylist(
        id = "akustik_santai",
        title = "Akustik Santai",
        subtitle = "Petikan gitar akustik & alunan syahdu penenang pikiran",
        trackCountText = "16 Lagu",
        badge = "AKUSTIK",
        tracks = listOf(
            track("Semua Aku Dirayakan", "Nadin Amizah"),
            track("Runtuh", "Feby Putri, Fiersa Besari"),
            track("Diri", "Tulus"),
            track("To the Bone", "Pamungkas"),
            track("Nanti Kita Seperti Ini", "Batas Senja"),
            track("Celengan Rindu", "Fiersa Besari"),
            track("Satu Bulan", "Bernadya"),
            track("Evaluasi", "Hindia"),
            track("Kenangan Manis", "Pamungkas"),
            track("Rumah Singgah", "Fabio Asher"),
            track("Satu-Satu", "Idgitaf"),
            track("Lantas", "Juicy Luicy"),
            track("Someone Like You", "Adele"),
            track("Stick Season", "Noah Kahan"),
            track("Yellow", "Coldplay"),
            track("I Wanna Be Yours", "Arctic Monkeys")
        )
    )
}
