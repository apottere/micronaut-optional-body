package optional.body

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("optional.body")
                .mainClass(Application.javaClass)
                .start()
    }
}