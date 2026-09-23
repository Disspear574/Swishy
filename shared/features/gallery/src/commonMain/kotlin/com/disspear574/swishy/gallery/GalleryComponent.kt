package com.disspear574.swishy.gallery

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.disspear574.swishy.media.AlbumKind
import com.disspear574.swishy.media.MonthKey

@OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
class GalleryComponent(componentContext: ComponentContext) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = Config.Months,
        handleBackButton = true,
        childFactory = { config, _ ->
            when (config) {
                Config.Months -> Child.Months
                Config.Trash -> Child.Trash
                Config.Settings -> Child.Settings
                Config.Duplicates -> Child.Duplicates
                is Config.Deck -> Child.Deck(config.source)
            }
        },
    )

    fun openMonth(month: MonthKey) {
        navigation.push(Config.Deck(DeckSource.Month(month)))
    }

    fun openUserAlbum(album: com.disspear574.swishy.media.UserAlbum) {
        navigation.push(Config.Deck(DeckSource.UserAlbum(id = album.id, title = album.title)))
    }

    fun openAlbum(album: AlbumKind) {
        navigation.push(Config.Deck(DeckSource.Album(album)))
    }

    fun openMix() {
        navigation.push(Config.Deck(DeckSource.Mix))
    }

    fun openTrash() {
        navigation.push(Config.Trash)
    }

    fun openDuplicates() {
        navigation.push(Config.Duplicates)
    }

    fun openSettings() {
        navigation.push(Config.Settings)
    }

    fun back() {
        navigation.pop()
    }

    sealed interface Config {
        data object Months : Config
        data object Trash : Config
        data object Settings : Config
        data object Duplicates : Config
        data class Deck(val source: DeckSource) : Config
    }

    sealed interface Child {
        data object Months : Child
        data object Trash : Child
        data object Settings : Child
        data object Duplicates : Child
        data class Deck(val source: DeckSource) : Child
    }
}
