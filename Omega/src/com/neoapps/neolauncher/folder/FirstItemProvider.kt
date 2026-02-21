package com.neoapps.neolauncher.folder

/*class FirstItemProvider(private val info: FolderInfo) : FolderInfo.FolderListener {

    var firstItem: ItemInfo? = findFirstItem()
        private set

    init {
        info.addListener(this)
    }

    private fun findFirstItem() = info.getContents().minByOrNull { it.rank }

    override fun onItemsChanged(animate: Boolean) {
        firstItem = findFirstItem()
    }

    override fun onAdd(item: ItemInfo?, rank: Int) = Unit
    override fun onRemove(item: MutableList<ItemInfo>?) = Unit
    override fun onTitleChanged(title: CharSequence?) = Unit
}*/