package com.example.data

import com.example.model.*

object QuickDropCatalog {

    val addresses = listOf(
        DeliveryAddress(
            id = "addr_indiranagar",
            label = "Home",
            fullAddress = "Flat 402, Green Glen Layout, Bellandur",
            areaShort = "Indiranagar, Bengaluru",
            hubName = "Indiranagar Hub",
            distanceKm = 0.8
        ),
        DeliveryAddress(
            id = "addr_koramangala",
            label = "Work",
            fullAddress = "3rd Floor, 80 Feet Road, Koramangala 4th Block",
            areaShort = "Koramangala, Bengaluru",
            hubName = "Koramangala Darkstore",
            distanceKm = 1.1
        ),
        DeliveryAddress(
            id = "addr_hsr",
            label = "Studio",
            fullAddress = "27th Main Road, Sector 1, HSR Layout",
            areaShort = "HSR Layout, Bengaluru",
            hubName = "HSR Express Hub",
            distanceKm = 0.6
        )
    )

    val categories = listOf(
        GroceryCategory(
            id = "dairy",
            name = "Dairy, Bread & Eggs",
            shortName = "Dairy, Bread\n& Eggs",
            iconKey = "breakfast_dining",
            hasSaleBadge = true,
            tintHex = 0xFFFFF8E1,
            subcategories = listOf("All", "Milk", "Bread & Pav", "Butter & Cheese", "Paneer", "Eggs", "Yogurt")
        ),
        GroceryCategory(
            id = "fruits_veg",
            name = "Fruits & Veggies",
            shortName = "Fruits &\nVeggies",
            iconKey = "nutrition",
            tintHex = 0xFFE8F5E9,
            subcategories = listOf("All", "Fresh Fruits", "Daily Veggies", "Exotics")
        ),
        GroceryCategory(
            id = "atta_rice",
            name = "Atta, Rice & Dal",
            shortName = "Atta, Rice\n& Dal",
            iconKey = "grain",
            tintHex = 0xFFF3EFEA,
            subcategories = listOf("All", "Atta & Flour", "Rice", "Dal & Pulses", "Ghee & Oil")
        ),
        GroceryCategory(
            id = "snacks",
            name = "Snacks & Munchies",
            shortName = "Snacks &\nMunchies",
            iconKey = "cookie",
            tintHex = 0xFFFFF3E0,
            subcategories = listOf("All", "Chips & Crisps", "Namkeen", "Biscuits")
        ),
        GroceryCategory(
            id = "drinks",
            name = "Cold Drinks & Juices",
            shortName = "Cold Drinks\n& Juices",
            iconKey = "local_bar",
            tintHex = 0xFFFFEBEE,
            subcategories = listOf("All", "Juices", "Soft Drinks", "Cold Coffee")
        ),
        GroceryCategory(
            id = "sweets",
            name = "Sweets & Ice Creams",
            shortName = "Sweets &\nIce Creams",
            iconKey = "icecream",
            tintHex = 0xFFF3E5F5,
            subcategories = listOf("All", "Ice Creams", "Chocolates", "Mithai")
        ),
        GroceryCategory(
            id = "instant",
            name = "Instant & Frozen",
            shortName = "Instant &\nFrozen",
            iconKey = "ramen_dining",
            tintHex = 0xFFFFFDE7,
            subcategories = listOf("All", "Noodles & Pasta", "Ready to Cook")
        ),
        GroceryCategory(
            id = "household",
            name = "Household Care",
            shortName = "Household\nCare",
            iconKey = "cleaning_services",
            tintHex = 0xFFE3F2FD,
            subcategories = listOf("All", "Detergents", "Cleaners", "Kitchen Rolls")
        )
    )

    val groceryPromos = listOf(
        PromoBanner(
            id = "promo_mango",
            tag = "SEASONAL PICK",
            title = "Summer Mango Fest",
            subtitle = "Sweet Alphonsos & Banganapalli up to 40% OFF",
            ctaText = "Shop Fresh Batch",
            targetCategoryId = "fruits_veg",
            bgHex = 0xFFF9BD00,
            contentDark = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAMsPF_F6DZjED4SeLSx4UHp7PpXweyGGK6hln8HHB625vgqqS9SvyE665FlhMhKRKp07u9PmKt8KlrD7LVgF-HI3bP6xHyU9KfMp3ht30aCzmC8g_FCezPiuDjk4BMcwsubkYP8a_YBOYHnN7XIopi_s4NVOgNZAVADf43ORnmS3J6hVdbRTOwpwIg88Uq3O6uuQuEIA0ns3bM4P-3rXMnEtuqMQ0LY9s7EtoUOK1e"
        ),
        PromoBanner(
            id = "promo_dairy",
            tag = "MORNING FRESH",
            title = "Dairy & Oven Bakery",
            subtitle = "Pure Cow Milk, Artisanal Pav & Malai Paneer",
            ctaText = "Explore Fresh Dairy",
            targetCategoryId = "dairy",
            bgHex = 0xFF0C831F,
            contentDark = false,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDE4Dl2U1st4av5lcaWIw7E3JPkd1NpSZnxODWZshol4SX_0NgbN6lbofLt5JO_r438x9nztb-CPv9JMdoT3CHknsoJyI38wyVrUtH047FPtb2boWPGD5Cqospscaey41LELCRLHsTAb27pVmu8efEIr-5019UjtVQ5N0HYgNIGEraMfgJd452Kh_Q8eN_uzLIl-Z81i7dQ2Hz_r0cLAbwHqmARGtCYNzDg8fx09qxV"
        ),
        PromoBanner(
            id = "promo_upi",
            tag = "UPI SPECIAL",
            title = "Super Saver Wednesday",
            subtitle = "Flat ₹100 instant savings on orders above ₹499",
            ctaText = "Use Code: QUICK100",
            targetCategoryId = null,
            bgHex = 0xFFD8E3FB,
            contentDark = true
        )
    )

    val foodPromos = listOf(
        PromoBanner(
            id = "food_promo_biryani",
            tag = "HOT & STEAMING",
            title = "60% OFF on Biryanis",
            subtitle = "Meghana Foods, Behrouz & top kitchens near you",
            ctaText = "Order in 22 Mins",
            targetCategoryId = "rest_meghana",
            isFoodPromo = true,
            bgHex = 0xFFE23744,
            contentDark = false
        ),
        PromoBanner(
            id = "food_promo_cafe",
            tag = "BENGALURU FAVS",
            title = "South Indian Breakfast",
            subtitle = "Ghee Podi Idli, Crispy Masala Dosa & Filter Kaapi",
            ctaText = "Explore Kitchens",
            targetCategoryId = "rest_rameshwaram",
            isFoodPromo = true,
            bgHex = 0xFFF9BD00,
            contentDark = true
        ),
        PromoBanner(
            id = "food_promo_pizza",
            tag = "GOURMET NIGHTS",
            title = "Woodfired Pizzas & Burgers",
            subtitle = "Buy 1 Get 1 on Artisan Sourdough Pizzas tonight",
            ctaText = "Claim Offer",
            targetCategoryId = "rest_brik_oven",
            isFoodPromo = true,
            bgHex = 0xFF1E293B,
            contentDark = false
        )
    )

    val products = listOf(
        // Dairy, Bread & Eggs
        GroceryProduct(
            id = "prod_amul_milk",
            brand = "Amul",
            name = "Amul Taaza Homogenised Toned Milk",
            categoryId = "dairy",
            subcategory = "Milk",
            variants = listOf(
                ProductVariant("amul_milk_500", "500 ml pouch", 27, 29),
                ProductVariant("amul_milk_1l", "1 L tetra", 54, 58)
            ),
            defaultVariantIndex = 0,
            etaMinutes = 10,
            isVeg = true,
            isBestseller = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCS3D5S1lR-23gyIJ3FqV4-26fytxLxTf9HJF0yubXXyNo34R-31nFVdYB4deWSU7asm1FmqeAMaoqHw1WbGTpvrwQ_GB0C8uZUmNMT_CWWpSFcmT_L3nBQHJL9eUmqMro6Lyc5Bm8pAWiewiiU2OeKYUDdbyQZZS0uvAaXb6B8nkYlnX78alEsJ1zrTqmjrkPHGhWnydE3327qhjTD1iuGOqCe_nSUf5-pdk2dv4XZ",
            tintHex = 0xFFEBF5FF
        ),
        GroceryProduct(
            id = "prod_amul_butter",
            brand = "Amul",
            name = "Amul Pasteurized Butter",
            categoryId = "dairy",
            subcategory = "Butter & Cheese",
            variants = listOf(
                ProductVariant("amul_butter_100", "100 g", 58, 60),
                ProductVariant("amul_butter_500", "500 g", 275, 290)
            ),
            defaultVariantIndex = 0,
            etaMinutes = 10,
            isVeg = true,
            isMissedQuickAdd = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAceGFkNFbnWLf3pc1C9YnsVy9sT5GD9eFrJKm7tBP42NHjN2-EMJrOV7teut9uN-DXoDIF0L27XkeInPS-RA_ojky3DfVG1SyjsQjaw934pwBefNUMTeCgoTkgBmncVxFYCTG-IpeKrmyxG_bPPuidHvLFpl-WQOYwLCLncyz8l3hSzccrI6lr6MDHz-wvyDcg0qhpyxl_v2_54aoXcB8TsI-rBLNHk7SS21hM8owK",
            tintHex = 0xFFFFF8DC
        ),
        GroceryProduct(
            id = "prod_nandini_milk",
            brand = "Nandini",
            name = "Nandini GoodLife Toned Milk",
            categoryId = "dairy",
            subcategory = "Milk",
            variants = listOf(
                ProductVariant("nandini_500", "500 ml", 28, 30),
                ProductVariant("nandini_1l", "1 L", 55, 60)
            ),
            etaMinutes = 10,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDFsJO8VPRXiaxvkGQQWQCpt9Y8uNlXL7dG0S2pWXn7_EcuMi3GrWEf9F05cLjmO55X27WzVu0_auMgGXGdGQcrTlpNkKIo79pFslu8rHeH2SsSGXww132vDcE5YgR6jm5Z9N1e_J4TFOF_3p3luza5oyA9li4I72WGR0e8H1y5YXNPu2gG6bYZvjTJ6YMfsOzHKYKlMTzEObdvtd-yKM2klUAn9VpwdRpcPSaWqVYE",
            tintHex = 0xFFE8F4FD
        ),
        GroceryProduct(
            id = "prod_malai_paneer",
            brand = "Farm Fresh",
            name = "Fresh Malai Paneer",
            categoryId = "dairy",
            subcategory = "Paneer",
            variants = listOf(
                ProductVariant("malai_paneer_200", "200 g block", 85, 95),
                ProductVariant("malai_paneer_400", "400 g family", 162, 190)
            ),
            etaMinutes = 10,
            badgeText = "Fresh Today",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCTrNjn4_kLMAYTmzP14v02Z1aNeKu3RII8qtZzonQRnZ1uL7Kev8jbWXxgN_M-UrWv0dx0p9bxYwfluDddjinztbOZvrsvYqZP6rWLEM1sEvsCdXsN8GMknXD3JUVGgbV22IAnnoOG0Z-geZGbnX173o3OqsMkdbcIm9e-XB0z8I6UQjY8o_HFVbxW1pbAmt36WzOwOUJjLoEgRGNAUh9uAR-GknFiQbKeGJSPO1C_",
            tintHex = 0xFFF3F9EE
        ),
        GroceryProduct(
            id = "prod_brown_bread",
            brand = "English Oven",
            name = "100% Whole Wheat Brown Bread",
            categoryId = "dairy",
            subcategory = "Bread & Pav",
            variants = listOf(
                ProductVariant("bread_400", "400 g", 45, 50),
                ProductVariant("bread_600", "600 g jumbo", 65, 75)
            ),
            etaMinutes = 10,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBH8RIfzNQx3Op94XzEtHtZbNuRnnf7UBnhCdEYwgs6xriMX-Ac4HY1dqshmeZqzVqEUx8W11q7bAlguRaZMJoeb1fyATCWjGxO4l_-myaN_z7M3l2V6dkzlrnH189uOlG4QbF048vUbqIAduZeoehz8KzZQWwdAxRnrNp8PciL0D7HfUWRxq4kuLrNmLATiX6RmOgf9A7-iqncoJhYuvQoujzSxRtmnAkn9wSSwWvz",
            tintHex = 0xFFFFF5EA
        ),
        GroceryProduct(
            id = "prod_eggoz_eggs",
            brand = "Eggoz",
            name = "Eggoz Farm Fresh White Eggs",
            categoryId = "dairy",
            subcategory = "Eggs",
            variants = listOf(
                ProductVariant("eggs_6", "Pack of 6", 58, 65),
                ProductVariant("eggs_10", "Pack of 10", 92, 110)
            ),
            etaMinutes = 10,
            isVeg = false,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBPYk0FxQf68i4vISEm6W4gsV4KiEp6n8y7o-Trd3pAGxTorEG2aPDiwfUFvxvlc0pP9L3a1yDENFJ3rulbedxEISOw9lAyxmgkGKAiUu7AcOLFfMFenNOnTRN6Q5IirNTEcAw5iu45mok030f4SkyVQt3fBN1SwFvptpfoy03B_p4iv73NrMHFiJTY_x_xGHx3n_RcA34s0UIvkrU27LiKSDjzjiut4eJU4g75kKZZ",
            tintHex = 0xFFFFF7EE
        ),
        GroceryProduct(
            id = "prod_epigamia_yogurt",
            brand = "Epigamia",
            name = "Epigamia Strawberry Greek Yogurt",
            categoryId = "dairy",
            subcategory = "Yogurt",
            variants = listOf(
                ProductVariant("epigamia_90", "90 g cup", 50, 60),
                ProductVariant("epigamia_400", "400 g tub", 185, 240)
            ),
            etaMinutes = 10,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDTghVkVS8k2PETzQ0au3fyPZo3Ay311yCGzumpoFn9xsPBZ68Zr09I3cEjbxFYOr5YCpxmq-_gFyqTQriTN23SvzfOOH9GXxW8pv8ZBHH57aBqh3C5in-NvZoBxvGJLiR4633-y_bOwpwvXaHyUmKaHPjX6kqChxvRw0FCc-UEEGfarj8ZBpsbk-5hiCNKoTJMlwXtPCsk7sHumgC9V3zyUN2GfbW-Qplx2LMGYfAB",
            tintHex = 0xFFFFEDF2
        ),
        GroceryProduct(
            id = "prod_mother_dairy_dahi",
            brand = "Mother Dairy",
            name = "Mother Dairy Classic Dahi / Curd",
            categoryId = "dairy",
            subcategory = "Yogurt",
            variants = listOf(
                ProductVariant("dahi_400", "400 g tub", 35, 38),
                ProductVariant("dahi_1kg", "1 kg bucket", 78, 88)
            ),
            etaMinutes = 10,
            isBestseller = true,
            isMissedQuickAdd = true,
            badgeText = "POPULAR",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD3eCcSZI6sctf0RqueLmBbLXn_3PDbYU3Ja4LRyUw_4Zm4ErWBDx2N7tJZKQqMCTwOv15kq2cVq7aByOjELBobxoFMqc6_dkj-n0GIT6ezxx-LDJFfWVZiSvlQfBVEpmpiGa1_dwvkp8dBKBZfeu0iSB9Xn_plAXMS361oGPu3ozjwMAww_E3R8mV45QNJsBn3piS3lYDYapp5_OwbhH2_IYYsz_ipq1h0kZsSMw3p",
            tintHex = 0xFFEAF6FF
        ),
        // Fruits & Veggies
        GroceryProduct(
            id = "prod_alphonso_mango",
            brand = "Farm Fresh",
            name = "Ratnagiri Alphonso Mangoes",
            categoryId = "fruits_veg",
            subcategory = "Fresh Fruits",
            variants = listOf(
                ProductVariant("mango_1kg", "1 kg (3-4 pcs)", 349, 450),
                ProductVariant("mango_500g", "500 g (2 pcs)", 185, 235)
            ),
            etaMinutes = 12,
            isBestseller = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAWA5Y3rwOxK-cL1VCWLTUh0mDlU8YTl468rJBJKpOTE6351ZOC1K05cuxxjGKz-TIGwhQfnGdozESKdlthTKj10C0dvzBJUGGG3Cr9bYLbPUYDN3QTigJ4ETpeCnJ14hyB3ZKNfbJTmVbruLZFPhlG7BqQLhl5mVP6qxI1oDD_GVDZhhjSrIlQSC-t4gm5b2unAM_SqSd7pbbkoVdSup4_kQ_GCzZwLMY-_1IJEQ5L",
            tintHex = 0xFFFFF4D9
        ),
        GroceryProduct(
            id = "prod_nasik_onions",
            brand = "Farm Fresh",
            name = "Fresh Nasik Red Onions",
            categoryId = "fruits_veg",
            subcategory = "Daily Veggies",
            variants = listOf(
                ProductVariant("onion_2kg", "2 kg net bag", 59, 90),
                ProductVariant("onion_1kg", "1 kg pack", 32, 48)
            ),
            etaMinutes = 9,
            isStealDeal = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCG5u0pWmhn4cPdYoqG4qtjQXLY8y9vWbaFvH2BQJgH5Zm5rWx_uAEAG2eLIHllAWFPY38bXKWjwTey2LSCx0NxrfXdpPENSA3qPrTrjc4f6Ji_iXZ0nQVtYtCDcxV7gl7pjE0RSwQehx6qXoI17BG2nraCIVQ7HAwp6mpMSFAQSWNA7LXDekIN7aqR5xPoPMH2sHigs425kmzYxjjKMDwxV74u-iwWxaRPPFJ9fq90",
            tintHex = 0xFFFCE8EC
        ),
        GroceryProduct(
            id = "prod_lemons",
            brand = "Farm Fresh",
            name = "Fresh Juicy Lemons (Nimbu)",
            categoryId = "fruits_veg",
            subcategory = "Daily Veggies",
            variants = listOf(
                ProductVariant("lemon_4", "4 pcs", 18, 25),
                ProductVariant("lemon_8", "8 pcs", 34, 48)
            ),
            etaMinutes = 9,
            isMissedQuickAdd = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDgl4XKyRPa-yKnslZgvj3A33PW2S0WocyqxnO4WA-Jd4tsj2m3e5mRTJ37AdHtsJefsJwtPeBSK11FI8CK7XID8f-P17-rQruoJCsbmxjlYZYG0Gn8LgBKJ1VEb_v0NInLf7np4RBcEv6S5tsuAFODMFA7xoFOjlj_ptkxo-QjD65de2IUYJ71vEmo94wpg9REaEC3Kqtv4Z0G8AWoTdwIw6Rvmg2VR_GRE_pLs9Td",
            tintHex = 0xFFFFFBE6
        ),
        GroceryProduct(
            id = "prod_shimla_apples",
            brand = "Himachal Orchard",
            name = "Royal Gala Shimla Apples",
            categoryId = "fruits_veg",
            subcategory = "Fresh Fruits",
            variants = listOf(
                ProductVariant("apple_500g", "500 g (4 pcs)", 119, 155),
                ProductVariant("apple_1kg", "1 kg", 229, 299)
            ),
            etaMinutes = 10,
            tintHex = 0xFFFFECEE
        ),
        // Atta, Rice & Dal
        GroceryProduct(
            id = "prod_aashirvaad_atta",
            brand = "Aashirvaad",
            name = "Aashirvaad Superior MP Shudh Chakki Atta",
            categoryId = "atta_rice",
            subcategory = "Atta & Flour",
            variants = listOf(
                ProductVariant("atta_5kg", "5 kg", 245, 290),
                ProductVariant("atta_10kg", "10 kg", 469, 560)
            ),
            etaMinutes = 11,
            isBestseller = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCyh340J_z8J9SgLyi3sbaCjF7vSVRGDXW5tK774F67e82DtdgupBouPUMR2Zd7Umzt_IsI9bzQl9RzFN4xg3ng4486AZdfF-1T8R2VbGp9OU0a8PWcPuit77uGoJybkZLbg7UASbYBvl2gE1TGmTjLOCe6Gt0VfzEl2_CItmMZAHIK00SJQ5FOlXUzUV0ZgGQhoDS8zzhGif48oqGrceN_37_QShLxfq0ucpeh_kDB",
            tintHex = 0xFFF7F0E6
        ),
        GroceryProduct(
            id = "prod_cow_ghee",
            brand = "Country Delight",
            name = "Country Delight Pure Cow Ghee",
            categoryId = "atta_rice",
            subcategory = "Ghee & Oil",
            variants = listOf(
                ProductVariant("ghee_1l", "1 L jar", 620, 710),
                ProductVariant("ghee_500ml", "500 ml jar", 325, 375)
            ),
            etaMinutes = 12,
            isBestseller = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDSFoK9joLyYtHJBzp3J-8WRV5wCRQtgPHx4uxC_XBWqLMmIdVMf3N6THIG4SroIlNt-gkGm3gmJAA-9-MP_lGmLp0fNhsgm_IpX25sc0LhucK6U_dXyoqsG1t5RK2XeaCUq5cIfG7daj6RXnvb-7YV3yAPgvPDnz4nA9tzuAyqEmqIjpJW_8ZDGGiY30ySiglEIL3vyJKqM17vLIC8HT9nkREgG8yFsXJS_NJGf5CG",
            tintHex = 0xFFFFF8DF
        ),
        GroceryProduct(
            id = "prod_basmati_rice",
            brand = "India Gate",
            name = "India Gate Classic Basmati Rice",
            categoryId = "atta_rice",
            subcategory = "Rice",
            variants = listOf(
                ProductVariant("rice_1kg", "1 kg", 145, 190),
                ProductVariant("rice_5kg", "5 kg", 699, 899)
            ),
            etaMinutes = 11,
            tintHex = 0xFFF2F5F8
        ),
        // Snacks & Munchies
        GroceryProduct(
            id = "prod_lays_masala",
            brand = "Lay's",
            name = "Lay's India's Magic Masala Chips",
            categoryId = "snacks",
            subcategory = "Chips & Crisps",
            variants = listOf(
                ProductVariant("lays_masala_50", "50 g bag", 20, 20),
                ProductVariant("lays_masala_90", "90 g party pack", 45, 50)
            ),
            etaMinutes = 9,
            isBestseller = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCraWvOHEm9Y0_luNT0HQ7Fx7KyDiBd5Q2yRkqALTLlR0GhRdlUms8axJ0mlJbfxCXiLbtkP2wo9200aX6_1ZhWBKr-JT8BWv5I2F7pkw3VS38sE35WgmG6sqoRxuULM2AccIDiIskJ5E7veSaf64jBjKTSdMj1LVb6vwfGDnfXhSMHqmHhBCSjlOGNZxtrQKRnWFnMP7oJqIvyC4GBB_PJv0Gn20v0Z-Ebqnb0fQxI",
            tintHex = 0xFFE6EFFF
        ),
        GroceryProduct(
            id = "prod_lays_salted",
            brand = "Lay's",
            name = "Lay's Classic Salted Potato Chips",
            categoryId = "snacks",
            subcategory = "Chips & Crisps",
            variants = listOf(
                ProductVariant("lays_salted_50", "50 g", 20, 20),
                ProductVariant("lays_salted_90", "90 g", 45, 50)
            ),
            etaMinutes = 9,
            isMissedQuickAdd = true,
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6uChA_hvVlxTb5QlTJh_NzCsIC_pZ9huggdZmhdsXB87QvyeNeAPDGc2T0Uzv8j-zA_Z6GdKXOHO9nh5A0xVQgMEKRhEg_PbSnfirrDeiea-27ZmTFoA3AInNVyrSl0K654KxTu77Zb6n65O38FfLYdgEJpRdO2zxIUmlu4MYQcJGwfY6RUDHPts4KNdpAoeqq0aZtfA2L8znfWJqGpBUyriD6v0yM0MBaeu2crJl",
            tintHex = 0xFFFFF8D6
        ),
        GroceryProduct(
            id = "prod_bhujia",
            brand = "Haldiram's",
            name = "Haldiram's Crispy Aloo Bhujia",
            categoryId = "snacks",
            subcategory = "Namkeen",
            variants = listOf(
                ProductVariant("bhujia_200", "200 g", 55, 65),
                ProductVariant("bhujia_400", "400 g", 105, 125)
            ),
            etaMinutes = 10,
            tintHex = 0xFFFFF0E0
        ),
        // Cold Drinks & Juices
        GroceryProduct(
            id = "prod_maaza_mango",
            brand = "Maaza",
            name = "Maaza Original Mango Drink",
            categoryId = "drinks",
            subcategory = "Juices",
            variants = listOf(
                ProductVariant("maaza_600", "600 ml", 38, 42),
                ProductVariant("maaza_1200", "1.2 L", 68, 85)
            ),
            etaMinutes = 9,
            tintHex = 0xFFFFF2CC
        ),
        GroceryProduct(
            id = "prod_cold_coffee",
            brand = "Sleepy Owl",
            name = "Classic Chilled Iced Latte",
            categoryId = "drinks",
            subcategory = "Cold Coffee",
            variants = listOf(
                ProductVariant("coffee_200", "200 ml", 65, 85),
                ProductVariant("coffee_pack4", "Pack of 4", 240, 340)
            ),
            etaMinutes = 10,
            tintHex = 0xFFF3ECE7
        ),
        // Sweets & Ice Creams
        GroceryProduct(
            id = "prod_amul_kulfi",
            brand = "Amul",
            name = "Amul Malai Kulfi Stick (Pack of 4)",
            categoryId = "sweets",
            subcategory = "Ice Creams",
            variants = listOf(
                ProductVariant("kulfi_4", "4 x 60 ml", 95, 120)
            ),
            etaMinutes = 10,
            tintHex = 0xFFFFF5E9
        ),
        GroceryProduct(
            id = "prod_dairy_milk",
            brand = "Cadbury",
            name = "Cadbury Dairy Milk Silk Roast Almond",
            categoryId = "sweets",
            subcategory = "Chocolates",
            variants = listOf(
                ProductVariant("silk_143", "143 g", 165, 195)
            ),
            etaMinutes = 9,
            tintHex = 0xFFF1E9FF
        ),
        // Instant & Frozen
        GroceryProduct(
            id = "prod_maggi",
            brand = "MAGGI",
            name = "MAGGI 2-Minute Masala Noodles",
            categoryId = "instant",
            subcategory = "Noodles & Pasta",
            variants = listOf(
                ProductVariant("maggi_4", "Pack of 4 (280g)", 56, 60),
                ProductVariant("maggi_12", "Family 12 Pack", 159, 180)
            ),
            etaMinutes = 9,
            tintHex = 0xFFFFF9D6
        ),
        // Household Care
        GroceryProduct(
            id = "prod_surf_excel",
            brand = "Surf Excel",
            name = "Surf Excel Matic Top Load Liquid",
            categoryId = "household",
            subcategory = "Detergents",
            variants = listOf(
                ProductVariant("surf_1l", "1 L bottle", 199, 245),
                ProductVariant("surf_2l", "2 L refill", 369, 460)
            ),
            etaMinutes = 11,
            tintHex = 0xFFE6F3FF
        )
    )

    val cuisineCravings = listOf(
        CuisineCraving("c_biryani", "Biryani", "rice_bowl", 0xFFFFEFE6),
        CuisineCraving("c_dosa", "South Indian", "bakery_dining", 0xFFFFF8DF),
        CuisineCraving("c_pizza", "Pizza", "local_pizza", 0xFFFFECEE),
        CuisineCraving("c_burger", "Burgers", "lunch_dining", 0xFFFFF3E0),
        CuisineCraving("c_dessert", "Desserts", "icecream", 0xFFF3E5F5),
        CuisineCraving("c_chai", "Chai & Cafe", "local_cafe", 0xFFE8F5E9)
    )

    val restaurants = listOf(
        Restaurant(
            id = "rest_meghana",
            name = "Meghana Foods",
            cuisines = "Biryani, Andhra, North Indian",
            rating = 4.6,
            ratingCountText = "12.4K+",
            deliveryTimeMins = 22,
            distanceKm = 1.6,
            priceForTwo = 550,
            offerBanner = "50% OFF up to ₹100",
            area = "Indiranagar 100ft Rd",
            isPureVeg = false,
            isPromoted = true,
            menu = listOf(
                MenuItem(
                    id = "dish_meghana_veg_biryani",
                    restaurantId = "rest_meghana",
                    restaurantName = "Meghana Foods",
                    name = "Meghana Special Veg Biryani",
                    description = "Aromatic long-grain basmati rice layered with spiced paneer, veggies, and signature Meghana masala.",
                    price = 285,
                    mrp = 320,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    isSpicy = true,
                    rating = 4.7,
                    ratingCount = "3.2K+"
                ),
                MenuItem(
                    id = "dish_meghana_chicken_biryani",
                    restaurantId = "rest_meghana",
                    restaurantName = "Meghana Foods",
                    name = "Boneless Chicken Biryani",
                    description = "Succulent boneless chicken pieces slow-cooked in Andhra spices served with raita and mirchi ka salan.",
                    price = 345,
                    mrp = 390,
                    category = "Recommended",
                    isVeg = false,
                    isBestseller = true,
                    isSpicy = true,
                    rating = 4.8,
                    ratingCount = "8.1K+"
                ),
                MenuItem(
                    id = "dish_meghana_gobi",
                    restaurantId = "rest_meghana",
                    restaurantName = "Meghana Foods",
                    name = "Crispy Gobi Manchurian",
                    description = "Wok-tossed crispy cauliflower florets glazed in garlic soy chili sauce.",
                    price = 215,
                    mrp = 240,
                    category = "Starters",
                    isVeg = true,
                    rating = 4.4,
                    ratingCount = "940+"
                ),
                MenuItem(
                    id = "dish_meghana_paneer_65",
                    restaurantId = "rest_meghana",
                    restaurantName = "Meghana Foods",
                    name = "Paneer 65 Fry",
                    description = "South-Indian style crispy tempered paneer cubes with curry leaves and green chilies.",
                    price = 260,
                    mrp = 290,
                    category = "Starters",
                    isVeg = true,
                    isSpicy = true,
                    rating = 4.5,
                    ratingCount = "1.1K+"
                )
            )
        ),
        Restaurant(
            id = "rest_rameshwaram",
            name = "The Rameshwaram Cafe",
            cuisines = "South Indian, Breakfast, Filter Coffee",
            rating = 4.5,
            ratingCountText = "18.2K+",
            deliveryTimeMins = 18,
            distanceKm = 0.9,
            priceForTwo = 250,
            offerBanner = "Flat ₹50 OFF above ₹199",
            area = "Indiranagar 12th Main",
            isPureVeg = true,
            isPromoted = true,
            menu = listOf(
                MenuItem(
                    id = "dish_ram_podi_idli",
                    restaurantId = "rest_rameshwaram",
                    restaurantName = "The Rameshwaram Cafe",
                    name = "Ghee Podi Thatte Idli",
                    description = "Steamed pillowy rice cake generously coated in roasted gunpowder podi and pure Nandini ghee.",
                    price = 115,
                    mrp = 130,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    rating = 4.8,
                    ratingCount = "6.5K+"
                ),
                MenuItem(
                    id = "dish_ram_masala_dosa",
                    restaurantId = "rest_rameshwaram",
                    restaurantName = "The Rameshwaram Cafe",
                    name = "Ghee Masala Dosa",
                    description = "Golden crispy dosa roasted in pure ghee with spiced potato palya and coconut chutney.",
                    price = 135,
                    mrp = 150,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    rating = 4.7,
                    ratingCount = "5.4K+"
                ),
                MenuItem(
                    id = "dish_ram_kaapi",
                    restaurantId = "rest_rameshwaram",
                    restaurantName = "The Rameshwaram Cafe",
                    name = "Authentic Filter Kaapi (250ml)",
                    description = "Rich Chikmagalur roast decoction frothed with farm milk in a thermal flask.",
                    price = 75,
                    mrp = 85,
                    category = "Beverages",
                    isVeg = true,
                    rating = 4.6,
                    ratingCount = "2.1K+"
                )
            )
        ),
        Restaurant(
            id = "rest_brik_oven",
            name = "Brik Oven Sourdough Pizza",
            cuisines = "Woodfired Pizza, Italian, Pastas",
            rating = 4.4,
            ratingCountText = "4.8K+",
            deliveryTimeMins = 26,
            distanceKm = 2.1,
            priceForTwo = 750,
            offerBanner = "60% OFF up to ₹120",
            area = "Indiranagar",
            isPureVeg = false,
            menu = listOf(
                MenuItem(
                    id = "dish_brik_margherita",
                    restaurantId = "rest_brik_oven",
                    restaurantName = "Brik Oven Sourdough Pizza",
                    name = "Original Neapolitan Margherita",
                    description = "48-hour fermented sourdough base, San Marzano tomato sauce, fresh buffalo mozzarella & basil.",
                    price = 425,
                    mrp = 495,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    rating = 4.6,
                    ratingCount = "1.9K+"
                ),
                MenuItem(
                    id = "dish_brik_pepperoni",
                    restaurantId = "rest_brik_oven",
                    restaurantName = "Brik Oven Sourdough Pizza",
                    name = "Smoked Chicken & Jalapeno Pizza",
                    description = "Woodfired sourdough pizza topped with smoked chicken, caramelized onions, and hot honey.",
                    price = 495,
                    mrp = 560,
                    category = "Woodfired Pizzas",
                    isVeg = false,
                    isSpicy = true,
                    rating = 4.5,
                    ratingCount = "890+"
                ),
                MenuItem(
                    id = "dish_brik_garlic_knots",
                    restaurantId = "rest_brik_oven",
                    restaurantName = "Brik Oven Sourdough Pizza",
                    name = "Truffle Butter Garlic Knots",
                    description = "Warm baked sourdough knots brushed with roasted garlic herb butter.",
                    price = 195,
                    mrp = 230,
                    category = "Sides",
                    isVeg = true,
                    rating = 4.4,
                    ratingCount = "610+"
                )
            )
        ),
        Restaurant(
            id = "rest_truffles",
            name = "Truffles Burger & Cafe",
            cuisines = "Burgers, American, Shakes",
            rating = 4.5,
            ratingCountText = "9.6K+",
            deliveryTimeMins = 24,
            distanceKm = 1.9,
            priceForTwo = 500,
            offerBanner = "40% OFF up to ₹80",
            area = "Koramangala 4th Block",
            isPureVeg = false,
            menu = listOf(
                MenuItem(
                    id = "dish_truffles_veg_burger",
                    restaurantId = "rest_truffles",
                    restaurantName = "Truffles Burger & Cafe",
                    name = "All American Veg Cheese Burger",
                    description = "Crispy herb potato & corn patty, melted cheddar, gherkins, and signature house sauce.",
                    price = 225,
                    mrp = 260,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    rating = 4.6,
                    ratingCount = "2.7K+"
                ),
                MenuItem(
                    id = "dish_truffles_chicken_burger",
                    restaurantId = "rest_truffles",
                    restaurantName = "Truffles Burger & Cafe",
                    name = "All American Chicken Burger",
                    description = "Double-crisp buttermilk fried chicken fillet with smoky chipotle mayo and slaw.",
                    price = 275,
                    mrp = 310,
                    category = "Recommended",
                    isVeg = false,
                    isBestseller = true,
                    rating = 4.7,
                    ratingCount = "4.2K+"
                ),
                MenuItem(
                    id = "dish_truffles_ferrero_shake",
                    restaurantId = "rest_truffles",
                    restaurantName = "Truffles Burger & Cafe",
                    name = "Ferrero Rocher Thick Shake",
                    description = "Velvety Belgian chocolate milkshake blended with roasted hazelnut crunch.",
                    price = 195,
                    mrp = 220,
                    category = "Shakes",
                    isVeg = true,
                    rating = 4.8,
                    ratingCount = "1.5K+"
                )
            )
        ),
        Restaurant(
            id = "rest_corner_house",
            name = "Corner House Ice Creams",
            cuisines = "Desserts, Ice Cream, Sundaes",
            rating = 4.8,
            ratingCountText = "15.1K+",
            deliveryTimeMins = 16,
            distanceKm = 0.7,
            priceForTwo = 300,
            offerBanner = "FREE Topping on Sundaes",
            area = "Indiranagar",
            isPureVeg = true,
            menu = listOf(
                MenuItem(
                    id = "dish_ch_dbc",
                    restaurantId = "rest_corner_house",
                    restaurantName = "Corner House Ice Creams",
                    name = "Death By Chocolate (DBC)",
                    description = "Bengaluru's iconic sundae: rich chocolate cake, vanilla ice cream, hot fudge sauce & roasted cashews.",
                    price = 230,
                    mrp = 250,
                    category = "Recommended",
                    isVeg = true,
                    isBestseller = true,
                    rating = 4.9,
                    ratingCount = "9.8K+"
                ),
                MenuItem(
                    id = "dish_ch_hot_fudge",
                    restaurantId = "rest_corner_house",
                    restaurantName = "Corner House Ice Creams",
                    name = "Hot Chocolate Fudge",
                    description = "Double scoop vanilla bean ice cream drenched in warm signature dark chocolate fudge.",
                    price = 175,
                    mrp = 190,
                    category = "Sundaes",
                    isVeg = true,
                    rating = 4.7,
                    ratingCount = "3.4K+"
                )
            )
        )
    )

    val coupons = listOf(
        Coupon(
            code = "QUICK100",
            title = "Flat ₹100 OFF",
            description = "Instant savings on orders above ₹349",
            discountAmount = 100,
            minOrder = 349
        ),
        Coupon(
            code = "WELCOME50",
            title = "Flat ₹50 OFF",
            description = "Applicable on all grocery & food orders above ₹199",
            discountAmount = 50,
            minOrder = 199
        ),
        Coupon(
            code = "PAYTMUPI",
            title = "₹25 UPI Cashback",
            description = "Instant discount via UPI on orders above ₹149",
            discountAmount = 25,
            minOrder = 149
        ),
        Coupon(
            code = "FREEDEL",
            title = "Free Delivery Pass",
            description = "Waives delivery partner fee on any cart value",
            discountAmount = 0,
            minOrder = 99,
            freeDelivery = true
        )
    )

    val deliveryInstructions = listOf(
        DeliveryInstruction("dont_ring", "Don't ring bell", "Baby / Pet asleep", "doorbell"),
        DeliveryInstruction("avoid_call", "Avoid Calling", "Send SMS/Push", "phone_disabled"),
        DeliveryInstruction("leave_door", "Leave at Door", "Drop & picture", "meeting_room"),
        DeliveryInstruction("leave_guard", "Leave with Guard", "Gate reception", "security")
    )

    val tipOptions = listOf(
        20 to "Warm",
        30 to "Sweet",
        50 to "Popular",
        100 to "Superstar"
    )

    val trendingSearches = listOf(
        "Alphonso Mangoes",
        "Amul Taaza Milk",
        "Meghana Biryani",
        "Ghee Podi Idli",
        "Death By Chocolate",
        "Brown Bread",
        "Sourdough Pizza",
        "Lay's Magic Masala"
    )
}
