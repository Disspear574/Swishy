package com.disspear574.swishy.gallery

import com.disspear574.swishy.media.MonthKey
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value

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
                is Config.Deck -> Child.Deck(config.month)
            }
        },
    )

    fun openMonth(month: MonthKey) {
        navigation.push(Config.Deck(month))
    }

    fun openMix() {
        navigation.push(Config.Deck(month = null))
    }

    fun openTrash() {
        navigation.push(Config.Trash)
    }

    fun back() {
        navigation.pop()
    }

    sealed interface Config {
        data object Months : Config
        data object Trash : Config
        data class Deck(val month: MonthKey?) : Config
    }

    sealed interface Child {
        data object Months : Child
        data object Trash : Child
        data class Deck(val month: MonthKey?) : Child
    }
}
