package com.example.data

fun listOfDefaultPlayers(): List<PlayerCard> = listOf(
    // Real Madrid (RMA)
    PlayerCard("rma_mbappe", "K. Mbappé", 92, "FWD", "Real Madrid", 97, 90, 80, 92, 36, 78, isMySquad = false, isUnlocked = false),
    PlayerCard("rma_vinicius", "Vinícius Jr.", 90, "FWD", "Real Madrid", 95, 84, 81, 91, 29, 68, isMySquad = false, isUnlocked = false),
    PlayerCard("rma_bellingham", "J. Bellingham", 90, "MID", "Real Madrid", 80, 85, 83, 88, 78, 82, isMySquad = false, isUnlocked = false),
    PlayerCard("rma_courtois", "T. Courtois", 90, "GK", "Real Madrid", 45, 20, 75, 48, 90, 85, isMySquad = false, isUnlocked = false),
    PlayerCard("rma_rudiger", "A. Rüdiger", 87, "DEF", "Real Madrid", 82, 50, 71, 69, 86, 86, isMySquad = false, isUnlocked = false),

    // Manchester City (MCI)
    PlayerCard("mci_haaland", "E. Haaland", 91, "FWD", "Manchester City", 89, 93, 66, 80, 45, 88, isMySquad = false, isUnlocked = false),
    PlayerCard("mci_debruyne", "K. De Bruyne", 90, "MID", "Manchester City", 72, 86, 94, 87, 65, 77, isMySquad = false, isUnlocked = false),
    PlayerCard("mci_rodri", "Rodri", 90, "MID", "Manchester City", 66, 80, 86, 82, 87, 85, isMySquad = false, isUnlocked = false),
    PlayerCard("mci_foden", "P. Foden", 89, "MID", "Manchester City", 86, 85, 86, 90, 57, 64, isMySquad = false, isUnlocked = false),
    PlayerCard("mci_dias", "Rúben Dias", 89, "DEF", "Manchester City", 62, 38, 69, 66, 89, 87, isMySquad = false, isUnlocked = false),

    // FC Bayern (FCB)
    PlayerCard("fcb_kane", "H. Kane", 90, "FWD", "FC Bayern", 69, 93, 84, 83, 47, 84, isMySquad = false, isUnlocked = false),
    PlayerCard("fcb_musiala", "J. Musiala", 87, "MID", "FC Bayern", 84, 79, 84, 90, 53, 59, isMySquad = false, isUnlocked = false),
    PlayerCard("fcb_kimmich", "J. Kimmich", 86, "MID", "FC Bayern", 68, 74, 88, 82, 81, 73, isMySquad = false, isUnlocked = false),
    PlayerCard("fcb_neuer", "M. Neuer", 86, "GK", "FC Bayern", 50, 22, 89, 55, 86, 80, isMySquad = false, isUnlocked = false),
    PlayerCard("fcb_davies", "A. Davies", 84, "DEF", "FC Bayern", 95, 68, 72, 83, 76, 77, isMySquad = false, isUnlocked = false),

    // FC Barcelona (BAR)
    PlayerCard("bar_lewandowski", "R. Lewandowski", 88, "FWD", "FC Barcelona", 73, 88, 70, 83, 43, 80, isMySquad = false, isUnlocked = false),
    PlayerCard("bar_yamal", "L. Yamal", 84, "FWD", "FC Barcelona", 87, 79, 80, 88, 32, 62, isMySquad = false, isUnlocked = false),
    PlayerCard("bar_pedri", "Pedri", 86, "MID", "FC Barcelona", 77, 71, 87, 87, 68, 64, isMySquad = false, isUnlocked = false),
    PlayerCard("bar_araujo", "R. Araújo", 86, "DEF", "FC Barcelona", 82, 48, 65, 62, 85, 84, isMySquad = false, isUnlocked = false),
    PlayerCard("bar_terstegen", "M. ter Stegen", 87, "GK", "FC Barcelona", 48, 21, 85, 52, 87, 81, isMySquad = false, isUnlocked = false),

    // PSG (Paris Saint-Germain)
    PlayerCard("psg_dembele", "O. Dembélé", 86, "FWD", "PSG", 93, 77, 83, 89, 33, 56, isMySquad = false, isUnlocked = false),
    PlayerCard("psg_marquinhos", "Marquinhos", 87, "DEF", "PSG", 78, 53, 73, 69, 88, 79, isMySquad = false, isUnlocked = false),
    PlayerCard("psg_donnarumma", "G. Donnarumma", 87, "GK", "PSG", 47, 18, 72, 50, 87, 83, isMySquad = false, isUnlocked = false),
    PlayerCard("psg_hakimi", "A. Hakimi", 84, "DEF", "PSG", 92, 75, 78, 82, 76, 73, isMySquad = false, isUnlocked = false),
    PlayerCard("psg_vitinha", "Vitinha", 85, "MID", "PSG", 74, 76, 84, 85, 70, 68, isMySquad = false, isUnlocked = false),

    // Liverpool (LIV)
    PlayerCard("liv_salah", "M. Salah", 89, "FWD", "Liverpool", 89, 87, 82, 88, 45, 75, isMySquad = false, isUnlocked = false),
    PlayerCard("liv_vandijk", "V. van Dijk", 89, "DEF", "Liverpool", 78, 60, 71, 72, 90, 86, isMySquad = false, isUnlocked = false),
    PlayerCard("liv_alisson", "Alisson", 89, "GK", "Liverpool", 49, 19, 84, 54, 89, 84, isMySquad = false, isUnlocked = false),
    PlayerCard("liv_macallister", "A. Mac Allister", 86, "MID", "Liverpool", 70, 80, 84, 84, 74, 76, isMySquad = false, isUnlocked = false),
    PlayerCard("liv_diaz", "L. Díaz", 84, "FWD", "Liverpool", 90, 79, 74, 86, 34, 69, isMySquad = false, isUnlocked = false),

    // Arsenal (ARS)
    PlayerCard("ars_saka", "B. Saka", 87, "FWD", "Arsenal", 86, 81, 82, 87, 56, 68, isMySquad = false, isUnlocked = false),
    PlayerCard("ars_odegaard", "M. Ødegaard", 89, "MID", "Arsenal", 75, 81, 89, 89, 58, 63, isMySquad = false, isUnlocked = false),
    PlayerCard("ars_saliba", "W. Saliba", 87, "DEF", "Arsenal", 80, 40, 72, 67, 87, 83, isMySquad = false, isUnlocked = false),
    PlayerCard("ars_rice", "D. Rice", 87, "MID", "Arsenal", 76, 70, 79, 79, 85, 83, isMySquad = false, isUnlocked = false),

    // In-game Pack Superstars
    PlayerCard("legend_messi", "L. Messi", 90, "FWD", "Inter Miami", 79, 87, 90, 92, 33, 64, isMySquad = false, isUnlocked = false),
    PlayerCard("legend_ronaldo", "C. Ronaldo", 88, "FWD", "Al Nassr", 77, 89, 78, 80, 34, 76, isMySquad = false, isUnlocked = false),
    PlayerCard("legend_neyma", "Neymar Jr.", 86, "FWD", "Al Hilal", 82, 81, 83, 90, 31, 58, isMySquad = false, isUnlocked = false),

    // Starting Ultimate Squad for player (unlocked & in squad by default)
    PlayerCard("start_jackson", "N. Jackson", 79, "FWD", "Ultimate FC", 84, 78, 70, 79, 38, 75, isMySquad = true, isUnlocked = true),
    PlayerCard("start_palmer", "C. Palmer", 82, "MID", "Ultimate FC", 78, 81, 82, 84, 45, 66, isMySquad = true, isUnlocked = true),
    PlayerCard("start_mainoo", "K. Mainoo", 78, "MID", "Ultimate FC", 75, 72, 76, 79, 72, 71, isMySquad = true, isUnlocked = true),
    PlayerCard("start_colwill", "L. Colwill", 77, "DEF", "Ultimate FC", 71, 45, 68, 66, 77, 78, isMySquad = true, isUnlocked = true),
    PlayerCard("start_vicario", "G. Vicario", 82, "GK", "Ultimate FC", 46, 20, 74, 50, 82, 80, isMySquad = true, isUnlocked = true),

    // More gold/silver bench players for unpack openings
    PlayerCard("gold_wirtz", "F. Wirtz", 87, "MID", "B. Leverkusen", 81, 78, 85, 89, 50, 60, isMySquad = false, isUnlocked = false),
    PlayerCard("gold_son", "H. Son", 86, "FWD", "Tottenham", 87, 86, 80, 84, 42, 69, isMySquad = false, isUnlocked = false),
    PlayerCard("gold_leao", "R. Leão", 86, "FWD", "AC Milan", 93, 79, 75, 87, 27, 74, isMySquad = false, isUnlocked = false),
    PlayerCard("gold_martinez", "L. Martínez", 87, "FWD", "Inter Milan", 79, 87, 71, 83, 48, 83, isMySquad = false, isUnlocked = false),
    PlayerCard("gold_valverde", "F. Valverde", 88, "MID", "Real Madrid", 88, 82, 84, 84, 80, 82, isMySquad = false, isUnlocked = false)
)
