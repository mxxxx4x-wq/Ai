package ae.streamvault.ui

import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import ae.streamvault.BuildConfig
import ae.streamvault.data.*
import kotlinx.coroutines.launch

@Composable fun StreamVaultApp() {
    var credentials by remember { mutableStateOf<Credentials?>(null) }
    var selected by remember { mutableStateOf<Pair<MediaItemDto, LibraryType>?>(null) }
    when {
        selected != null -> PlayerScreen(credentials!!, selected!!.first, selected!!.second) { selected = null }
        credentials == null -> LoginScreen { credentials = it }
        else -> HomeScreen(credentials!!) { item, type -> selected = item to type }
    }
}

@Composable private fun LoginScreen(onLogin: (Credentials) -> Unit) {
    var server by remember { mutableStateOf("") }; var user by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }; var error by remember { mutableStateOf<String?>(null) }; var busy by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Card(Modifier.widthIn(max = 520.dp).padding(24.dp)) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("StreamVault XT", style = MaterialTheme.typography.headlineMedium); Text("Use credentials from a service you are authorized to access.")
            OutlinedTextField(server, { server = it }, label = { Text("Server URL, e.g. https://provider.example:443") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(user, { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = !busy && server.isNotBlank() && user.isNotBlank() && pass.isNotBlank(), onClick = { scope.launch { busy = true; error = null; try { val c = Credentials(server, user, pass); val r = ApiFactory.xtream(c.base).login(user, pass); if (r.userInfo?.auth == 1) onLogin(c) else error = "Login rejected by server" } catch (e: Exception) { error = e.message ?: "Connection failed" } finally { busy = false } } }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Connecting..." else "Sign in") }
        }}
    }
}

@Composable private fun HomeScreen(c: Credentials, onPlay: (MediaItemDto, LibraryType) -> Unit) {
    var type by remember { mutableStateOf(LibraryType.MOVIES) }; var media by remember { mutableStateOf<List<MediaItemDto>>(emptyList()) }; var query by remember { mutableStateOf("")}; var error by remember { mutableStateOf<String?>(null) }; val scope = rememberCoroutineScope()
    fun load(t: LibraryType) { type = t; scope.launch { try { media = ApiFactory.xtream(c.base).media(c.username, c.password, t.action); error = null } catch (e: Exception) { error = e.message } } }
    LaunchedEffect(Unit) { load(type) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { LibraryType.entries.forEach { t -> FilterChip(selected = type == t, onClick = { load(t) }, label = { Text(t.name) }) } }
        Spacer(Modifier.height(12.dp)); OutlinedTextField(query, { query = it }, label = { Text("Search ${type.name.lowercase()}") }, modifier = Modifier.fillMaxWidth()); error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        val shown = media.filter { it.name.contains(query, true) }
        LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), contentPadding = PaddingValues(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(shown, key = { "${type.name}-${it.id}" }) { item -> Card(Modifier.clickable { onPlay(item, type) }) { Column { Image(rememberAsyncImagePainter(item.poster), null, Modifier.fillMaxWidth().aspectRatio(.68f), contentScale = ContentScale.Crop); Column(Modifier.padding(10.dp)) { Text(item.name, maxLines = 2); if (type != LibraryType.LIVE) Text("IMDb enrichment on details", style = MaterialTheme.typography.labelSmall) } } } }
        }
    }
}

@Composable private fun PlayerScreen(c: Credentials, item: MediaItemDto, type: LibraryType, back: () -> Unit) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    var meta by remember { mutableStateOf<ImdbMeta?>(null) }
    DisposableEffect(Unit) { onDispose { player.release() } }
    LaunchedEffect(item.id) {
        if (type != LibraryType.SERIES) { val ext = item.extension ?: if (type == LibraryType.LIVE) "m3u8" else "mp4"; player.setMediaItem(MediaItem.fromUri(c.streamUrl(type.path, item.id, ext))); player.prepare(); player.playWhenReady = true }
        if (type != LibraryType.LIVE) runCatching { ApiFactory.metadata(BuildConfig.METADATA_BASE_URL).metadata(item.name, item.releaseDate?.take(4)?.toIntOrNull(), if (type == LibraryType.MOVIES) "movie" else "series") }.onSuccess { meta = it }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = back) { Text("Back") }; Text(meta?.title ?: item.name, style = MaterialTheme.typography.titleLarge); meta?.rating?.let { Text("IMDb $it/10") } }
        if (type == LibraryType.SERIES) Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("Series catalog loaded. Add get_series_info episode selection before playback.\n${meta?.plot ?: item.plot.orEmpty()}") }
        else AndroidView(factory = { PlayerView(it).apply { this.player = player; layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT) } }, modifier = Modifier.fillMaxSize())
    }
}
