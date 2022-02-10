package io.github.yemyatthu1990.apm.instrumentations

class InstrumentTracer {
    companion object {
        fun enterMethodPrint(name: String) {
            println("entering "+ name+ " method at "+ System.currentTimeMillis())
        }

        fun exitMethodPrint(name: String) {
            println("exiting "+ name+ " method at "+ System.currentTimeMillis())
        }
    }
}