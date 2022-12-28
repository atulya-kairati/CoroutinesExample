package com.atulya.coroutinesexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atulya.coroutinesexample.databinding.ActivityMainBinding
import com.atulya.coroutinesexample.model.SomeViewModel
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope


const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: SomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        Log.d(TAG, "This is in UI thread: ${Thread.currentThread().name}")

        /**
         * If the main thread is destroyed or finishes,
         * then all the coroutines present will be destroyed
         * as well, doesn't matter if they they have finished or not.
         *
         * [GlobalScope] can cause memory leaks.
         * Use [lifecycleScope] and [viewModelScope] where ever possible.
         */

//        simpleCoroutine()

//        coroutineWithContext()

//        coroutineOnCustomThread()

//        switchingCoroutineContext()

//        runBlockingCoroutines()

//        waitingForCoroutineToFinish()

//        cancellationWhenCoroutineIsNotBusy()

//        cancellationWhenCoroutineIsBusy()

//        cancellationOfCoroutineWithTimeout()

//        usingAsyncAwait()

//        coroutineWithLifecycleScope()
        
        coroutineWithViewModelScope()

        

    }

    private suspend fun doNetworkCall1(): String {

        /**
         * A suspending function is simply a function
         * that can be paused and resumed at a later time.
         * They can execute a long running operation and
         * wait for it to complete without blocking.
         * A suspend function can only be called
         * from other suspend function or coroutine.
         */

        delay(2000)
        return "Google"
    }

    private suspend fun doNetworkCall2(): String {
        delay(3000)
        return "Yandex"
    }

    private fun simpleCoroutine() {
        GlobalScope.launch {

            /**
             * [GlobalScope] means that it will be only destroyed
             * either if it finishes or if the main thread has
             * ended.
             */

            val res1 = doNetworkCall1()
            Log.d(TAG, "onCreate: Response 1: $res1")

            val res2 = doNetworkCall2()
            Log.d(TAG, "onCreate: Response 2: $res2")
        }
    }

    private fun coroutineWithContext() {
        // Coroutines with context (Dispatchers)
        GlobalScope.launch(Dispatchers.Main) {
            /**
             * [Dispatchers.Main]
             * To interact with the main thread.
             * Like updating the UI.
             * It is non blocking.
             *
             * [Dispatchers.IO]
             * For data(I/O) operations.
             * Like networking, databases, Files, etc.
             *
             * [Dispatchers.Default]
             * For long running calculations.
             * Like sorting a very long list.
             *
             * [Dispatchers.Unconfined]
             * Not confined to any specific thread.
             * Can be used to hop between different threads.
             */
        }
    }

    private fun coroutineOnCustomThread() {
        // Coroutine on a custom thread
        GlobalScope.launch(newSingleThreadContext("MyThread")) {

        }
    }

    private fun switchingCoroutineContext() {
        // Switching Coroutine context
        GlobalScope.launch(Dispatchers.IO) {
            // First do an IO operation (e.g. networking)
            val res = doNetworkCall1()

            // Then we switch the context
            // to be able to manipulate the
            // Main UI
            withContext(Dispatchers.Main) {
                binding.textView.text = res
            }
        }
    }

    private fun runBlockingCoroutines() {
        // runBlocking
        runBlocking {
            /**
             * [runBlocking] can be used to run a coroutine
             * or a suspend function on the main(UI) thread.
             * It is blocking.
             */
            Log.d(TAG, "runBlocking: Start on thread ${Thread.currentThread().name}")
            delay(2000) // blocks the UI thread for 2secs
            Log.d(TAG, "runBlocking: still on thread ${Thread.currentThread().name}")

            // We can also call suspend functions
            binding.textView.text = doNetworkCall2()

            Log.d(TAG, "runBlocking: still on thread ${Thread.currentThread().name}")

            // we can also start new coroutines
            launch(Dispatchers.IO) {
                /**
                 * This will be asynchronous in nature.
                 * Will run on different thread than UI.
                 */

                Log.d(
                    TAG,
                    "runBlocking: New coroutine 1: Now on thread ${Thread.currentThread().name}"
                )

                delay(5000)
                Log.d(TAG, "Coroutine 1 finished")
            }

            launch(Dispatchers.IO) {
                Log.d(
                    TAG,
                    "runBlocking: New coroutine 2: Now on thread ${Thread.currentThread().name}"
                )

                delay(3000)
                Log.d(TAG, "Coroutine 2 finished")
            }

            /**
             * If 2 coroutines are created in runBlocking
             * 1st takes 2 secs to finish
             * 2nd takes 3 secs to finish
             *
             * The blocking time won't add up to 5secs,
             * but it will block for whole 3 secs.
             */
        }
    }

    private fun waitingForCoroutineToFinish() {
        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Starting.")
            repeat(5) {
                Log.d(TAG, "Coroutine going on: $it")
                delay(1000)
            }
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Finished.")
        }

        runBlocking {
            job.join() // join is used to wait for a coroutine
            Log.d(TAG, "waitingForCoroutineToFinish: Main thread continuing")
        }

    }

    private fun cancellationWhenCoroutineIsNotBusy() {
        /**
         * If a coroutine has lots of delay,
         * then it can be easily cancelled.
         */

        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Starting.")
            repeat(5) {
                Log.d(TAG, "Coroutine going on: $it")
                delay(1000)
            }
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Finished.")
        }

        runBlocking {
            delay(2000)
            // Cancelling the job after 2 secs
            job.cancel() // cancel is used to cancelling for a coroutine
            Log.d(TAG, "coroutineCancellation1: Coroutine cancelled")
            Log.d(TAG, "waitingForCoroutineToFinish: Main thread continuing")
        }

    }

    private fun cancellationWhenCoroutineIsBusy() {
        /**
         * If a coroutine is very busy,
         * then it can't be easily cancelled.
         *
         * We have to actively check if a coroutine
         * has been marked for cancellation and only
         * continue the coroutine has not been cancelled.
         */

        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Starting.")

            for (i in 35..45) {

                if (isActive) { // we check if coroutine is still active
                    Log.d(TAG, "coroutineCancellation2: i = $i: ${fib(i)}")
                }

            }

            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Finished.")
        }

        runBlocking {
            delay(2000)
            // Cancelling the job after 2 secs
            job.cancel() // cancel is used to cancelling for a coroutine
            Log.d(TAG, "coroutineCancellation1: Coroutine cancelled")
            Log.d(TAG, "waitingForCoroutineToFinish: Main thread continuing")
        }


    }

    private fun fib(n: Int): Int = when (n) {
        0 -> 0
        1 -> 1
        else -> fib(n - 1) + fib(n - 2)
    }

    private fun cancellationOfCoroutineWithTimeout(){
        /**
         * Sometimes we need to cancel a job
         * after sometime.
         * Ex. While making a network call
         * we can't wait indefinitely.
         */

        GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Starting.")

            withTimeout(3000) {
                // This will cancel the job after 3 secs

                for (i in 35..45) {

                    if (isActive) { // we check if coroutine is still active
                        Log.d(TAG, "coroutineCancellation2: i = $i: ${fib(i)}")
                    }
                    else{
                        Log.d(TAG, "cancellationOfCoroutineWithTimeout: Cancelled")
                    }

                }
            }

            Log.d(TAG, "waitingForCoroutineToFinish: Coroutine Finished.")
        }
    }

    private fun usingAsyncAwait(){
        /**
         * If we are lets say making few network calls in a coroutine
         * then they will be executed in a sequential manner but in
         * practice we want them to execute all at once
         *
         * we can use async{} in a coroutine to execute all network calls
         * at once. async{} returns a [Deferred] which can be awaited on.
         */


        // without async
        GlobalScope.launch(Dispatchers.IO) {
            val timeWithoutAsync = measureTimeMillis { // used to measure time the code inside took

                val res1 = doNetworkCall1()
                val res2 = doNetworkCall2()

                Log.d(TAG, "No async: Response1: $res1")
                Log.d(TAG, "No async: Response2: $res2")

            }

            Log.d(TAG, "usingAsyncAwait: Time taken without async: $timeWithoutAsync ms")
        }

        // with async
        GlobalScope.launch(Dispatchers.IO) {


            val timeWithAsync = measureTimeMillis { // used to measure time the code inside took

                val res1 = async { doNetworkCall1() }
                val res2 = async { doNetworkCall2() }

                // ONLY AWAIT AFTER ALL THE ASYNC CALLS
                Log.d(TAG, "Async: Response1: ${res1.await()}")
                Log.d(TAG, "Async: Response2: ${res2.await()}")

            }

            Log.d(TAG, "usingAsyncAwait: Time taken with async: $timeWithAsync ms")

        }
    }

    private fun coroutineWithLifecycleScope(){
        /**
         * Using Coroutine with [GlobalScope] can cause memory leaks
         * Since the [GlobalScope] coroutine will run until the app runs.
         * Even though activity associated with the coroutine might be gone
         *
         * So we use [lifecycleScope] to end a coroutine once the activity
         * has been destroyed.
         */

        binding.button.setOnClickListener{
            lifecycleScope.launch {

                /**
                 * If the below loop was in [GlobalScope], then
                 * it would have ran indefinitely until the app
                 * was closed.
                 *
                 * But since we used [lifecycleScope], this will stop once the
                 * Activity is destroyed
                 *
                 *  -> Change the [lifecycleScope] -> [GlobalScope] to observe
                 *
                 *  **NOTE**
                 *  This will NOT SURVIVE activity recreation e.g. orientation change.
                 *  use [viewModelScope] for that
                 */

                while (true){
                    // This loop will run forever
                    delay(1000)
                    Log.d(TAG, "coroutineWithLifecycleScope: still running....")
                }
            }

            GlobalScope.launch {

                // Even if the activity is recreated or destroyed this will remain active.
                delay(5000)
                Intent(this@MainActivity, SecondActivity::class.java).also {
                    startActivity(it)
                    finish() // destroys the current activity
                }
            }

        }
    }
    
    private fun coroutineWithViewModelScope(){
        /**
         * Using Coroutine with [GlobalScope] can cause memory leaks
         * Since the [GlobalScope] coroutine will run until the app runs.
         * Even though activity associated with the coroutine might be gone
         *
         * So we use [lifecycleScope] to end a coroutine once the activity
         * has been destroyed.
         *
         * But if we want our co routine to survive activity recreation then,
         * we can use [viewModelScope].
         */

        binding.button.setOnClickListener{
            viewModel.viewModelScope.launch {

                /**
                 * This will survive activity recreation unlike
                 * if we used [lifecycleScope].
                 */

                while (true){
                    // This loop will run forever
                    delay(1000)
                    Log.d(TAG, "coroutineWithLifecycleScope: still running....")
                }
            }

            GlobalScope.launch {

                // Even if the activity is recreated or destroyed this will remain active.
                delay(5000)
                Intent(this@MainActivity, SecondActivity::class.java).also {
                    startActivity(it)
                    finish() // destroys the current activity
                }
            }

        }
    }
}
