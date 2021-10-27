package hu.bme.aut.android.turisztikapp.data

enum class Category {
    Museum, Library, ArtGallery, Church, Zoo, Castle;

    companion object {

        fun getByOrdinal(ordinal: Int): Category? {
            var category: Category? = null
            for (cat in values()) {
                if (cat.ordinal == ordinal) {
                    category = cat
                    break
                }
            }
            return category
        }

        /* fun toInt(category: Category): Int {
             return category.ordinal
         }*/

    }

}