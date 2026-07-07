package com.example.data.repository

import com.example.data.remote.ProductDto

object SampleData {
    val products: List<ProductDto> by lazy {
        val list = mutableListOf<ProductDto>()
        var idCounter = 100 // Start higher to avoid collision if mixed with API

        // --- FOOTWEAR (Sneakers, Boots, Sandals, etc) ---
        val shoeBrands = listOf("Nike", "Adidas", "Puma", "Reebok", "Bata", "GoldStar", "Converse")
        val shoeTypes = listOf("Running Shoes", "Sneakers", "Formal Leather Shoes", "Summer Sandals", "Hiking Boots", "Canvas Shoes")
        val shoeColors = listOf("White", "Black", "Navy Blue", "Crimson", "Grey", "Brown")
        
        for (brand in shoeBrands) {
            for (type in shoeTypes) {
                for (color in shoeColors) {
                    val price = (40..450).random().toDouble() + 0.99
                    list.add(ProductDto(
                        id = idCounter++,
                        title = "$brand $color $type",
                        price = price,
                        description = "High-quality $type from $brand in a stylish $color. Perfect for daily wear and extreme comfort.",
                        category = "footwear",
                        image = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80"
                    ))
                }
            }
        }

        // --- ELECTRONICS (Gadgets, Accessories) ---
        val techItems = listOf(
            Triple("Wireless Earbuds", "electronics", 25.0),
            Triple("Bluetooth Speaker", "electronics", 45.0),
            Triple("Mechanical Keyboard", "electronics", 89.0),
            Triple("Gaming Mouse", "electronics", 35.0),
            Triple("USB-C Fast Charger", "electronics", 15.0),
            Triple("Power Bank 20000mAh", "electronics", 55.0),
            Triple("Smart LED Bulb", "electronics", 12.0),
            Triple("Noise Cancelling Headphones", "electronics", 199.0)
        )

        for (item in techItems) {
            for (i in 1..10) {
                val variant = if (i % 2 == 0) "Pro" else "Elite"
                val price = item.third * (0.8 + (i * 0.1))
                list.add(ProductDto(
                    id = idCounter++,
                    title = "PasalHub ${item.first} $variant V$i",
                    price = price,
                    description = "Latest generation ${item.first} featuring advanced connectivity and premium build quality for professional users.",
                    category = item.second,
                    image = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"
                ))
            }
        }

        // --- CLOTHING (Fashion) ---
        val clothes = listOf("Cotton T-Shirt", "Slim Fit Jeans", "Hooded Sweatshirt", "Summer Dress", "Formal Blazer")
        val clothMaterials = listOf("Organic Cotton", "Premium Denim", "Soft Wool", "Linen Blend")
        
        for (item in clothes) {
            for (material in clothMaterials) {
                for (size in listOf("S", "M", "L", "XL")) {
                    val price = (15..120).random().toDouble() + 0.50
                    list.add(ProductDto(
                        id = idCounter++,
                        title = "$material $item ($size)",
                        price = price,
                        description = "Comfortable $item made from $material. Available in $size size. Breathable fabric for all-day wear.",
                        category = "clothing",
                        image = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80"
                    ))
                }
            }
        }

        // --- HOME & APPLIANCES ---
        val homeItems = listOf("Blender", "Air Purifier", "Toaster", "Electric Kettle", "Vacuum Cleaner", "Table Lamp")
        for (item in homeItems) {
            for (i in 1..8) {
                val price = (30..250).random().toDouble() + 0.95
                list.add(ProductDto(
                    id = idCounter++,
                    title = "HomeMaster $item Modern Edition",
                    price = price,
                    description = "Essential $item for your modern home. Energy efficient and easy to use design.",
                    category = "home_appliances",
                    image = "https://images.unsplash.com/photo-1527443154391-507e9dc6c5cc?auto=format&fit=crop&w=600&q=80"
                ))
            }
        }

        // --- JEWELERY & ACCESSORIES ---
        val jewelry = listOf("Gold Plated Necklace", "Silver Ring", "Leather Wallet", "Polarized Sunglasses", "Silk Scarf")
        for (item in jewelry) {
            for (i in 1..10) {
                val price = (10..500).random().toDouble()
                list.add(ProductDto(
                    id = idCounter++,
                    title = "Elegance Collection: $item #$i",
                    price = price,
                    description = "Exquisite $item from our premium Elegance Collection. Hand-crafted for perfection.",
                    category = if (item.contains("Wallet") || item.contains("Sunglasses") || item.contains("Scarf")) "accessories" else "jewelery",
                    image = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=600&q=80"
                ))
            }
        }

        // --- SPORTS & FITNESS ---
        val sports = listOf("Yoga Mat", "Dumbbell Set", "Basketball", "Tennis Racket", "Skipping Rope")
        for (item in sports) {
            for (i in 1..10) {
                val price = (5..80).random().toDouble() + 0.25
                list.add(ProductDto(
                    id = idCounter++,
                    title = "FitPro $item Series $i",
                    price = price,
                    description = "Professional grade $item to boost your workout performance. Durable and ergonomic.",
                    category = "sports_fitness",
                    image = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=600&q=80"
                ))
            }
        }

        list.shuffle()
        list.take(350) // Ensure at least 300+
    }
}
