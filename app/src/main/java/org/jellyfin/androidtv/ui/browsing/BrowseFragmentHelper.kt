package org.jellyfin.androidtv.ui.browsing

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import org.koin.java.KoinJavaComponent
import java.util.UUID

/**
 * Fetch a [BaseItemDto] by ID.
 */
suspend fun fetchItem(api: ApiClient, itemId: UUID): BaseItemDto? = withContext(Dispatchers.IO) {
	runCatching {
		val response by api.userLibraryApi.getItem(itemId)
		response
	}.getOrNull()
}

/**
 * Launch async initialization for [EnhancedBrowseFragment].
 * Fetches the folder item by ID, then runs setupQueries on the main thread.
 */
fun EnhancedBrowseFragment.launchSetup() {
	val folderId = arguments?.getString(Extras.Folder) ?: return
	val api = KoinJavaComponent.get<ApiClient>(ApiClient::class.java)

	lifecycleScope.launch {
		val item = fetchItem(api, UUID.fromString(folderId))
		if (item != null) {
			mFolder = item
			applyCollectionType()
		}
		setupQueries(this@launchSetup)
	}
}

private fun EnhancedBrowseFragment.applyCollectionType() {
	val ct = mFolder?.collectionType ?: run {
		showViews = false
		return
	}
	when (ct) {
		CollectionType.MOVIES -> itemType = BaseItemKind.MOVIE
		CollectionType.TVSHOWS -> itemType = BaseItemKind.SERIES
		CollectionType.MUSIC -> itemType = BaseItemKind.MUSIC_ALBUM
		CollectionType.FOLDERS -> showViews = false
		else -> showViews = false
	}
}

/**
 * Launch async folder fetch for [BrowseGridFragment].
 * Fetches the item by ID, then calls [BrowseGridFragment.onFolderLoaded] on the main thread.
 */
fun BrowseGridFragment.launchFolderFetch() {
	val folderId = arguments?.getString(Extras.Folder) ?: return
	val api = KoinJavaComponent.get<ApiClient>(ApiClient::class.java)

	lifecycleScope.launch {
		val item = fetchItem(api, UUID.fromString(folderId)) ?: return@launch
		onFolderLoaded(item)
	}
}
