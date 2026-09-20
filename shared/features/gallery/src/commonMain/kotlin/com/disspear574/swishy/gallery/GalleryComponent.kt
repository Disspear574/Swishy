package com.disspear574.swishy.gallery

import com.disspear574.swishy.media.MonthKey
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value

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
                is Config.Deck -> Child.Deck(config.month)
            }
        },
    )

    fun openMonth(month: MonthKey) {
        navigation.push(Config.Deck(month))
    }

    fun back() {
        navigation.pop()
    }

    sealed interface Config {
        data object Months : Config
        data class Deck(val month: MonthKey) : Config
    }

    sealed interface Child {
        data object Months : Child
        data class Deck(val month: MonthKey) : Child
    }
}
