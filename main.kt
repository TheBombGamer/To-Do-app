import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class Task(var title: String, var description: String, var dueDate: LocalDate, var priority: String, var category: String, var isComplete: Boolean = false)

class ToDoList {
    private val tasks = mutableListOf<Task>()
    private val gson = Gson()
    private val file = File("tasks.json")

    init {
        loadTasks()
    }

    fun addTask(title: String, description: String, dueDate: LocalDate, priority: String, category: String) {
        tasks.add(Task(title, description, dueDate, priority, category))
        saveTasks()
    }

    fun editTask(index: Int, title: String, description: String, dueDate: LocalDate, priority: String, category: String) {
        if (index in tasks.indices) {
            tasks[index] = Task(title, description, dueDate, priority, category, tasks[index].isComplete)
            saveTasks()
        }
    }

    fun deleteTask(index: Int) {
        if (index in tasks.indices) {
            tasks.removeAt(index)
            saveTasks()
        }
    }

    fun markTaskAsComplete(index: Int) {
        if (index in tasks.indices) {
            tasks[index].isComplete = true
            saveTasks()
        }
    }

    fun viewTasks(): List<Task> {
        return tasks
    }

    fun searchTasks(query: String): List<Task> {
        return tasks.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    fun sortTasksByDueDate(): List<Task> {
        return tasks.sortedBy { it.dueDate }
    }

    fun sortTasksByPriority(): List<Task> {
        val priorityOrder = mapOf("High" to 1, "Medium" to 2, "Low" to 3)
        return tasks.sortedBy { priorityOrder[it.priority] }
    }

    private fun saveTasks() {
        file.writeText(gson.toJson(tasks))
    }

    private fun loadTasks() {
        if (file.exists()) {
            val type = object : TypeToken<List<Task>>() {}.type
            tasks.addAll(gson.fromJson(file.readText(), type))
        }
    }
}

class ToDoApp : Application() {
    private val toDoList = ToDoList()
    private lateinit var taskListView: ListView<String>

    override fun start(primaryStage: Stage) {
        val titleField = TextField()
        val descriptionField = TextField()
        val dueDatePicker = DatePicker()
        val priorityComboBox = ComboBox<String>().apply {
            items.addAll("Low", "Medium", "High")
        }
        val categoryField = TextField()
        val addButton = Button("Add Task")
        val searchField = TextField()
        val searchButton = Button("Search")
        taskListView = ListView()

        addButton.setOnAction {
            val title = titleField.text
            val description = descriptionField.text
            val dueDate = dueDatePicker.value
            val priority = priorityComboBox.value ?: "Low"
            val category = categoryField.text
            toDoList.addTask(title, description, dueDate, priority, category)
            updateTaskList()
        }

        searchButton.setOnAction {
            val query = searchField.text
            val results = toDoList.searchTasks(query)
            taskListView.items.clear()
            results.forEach { task -> taskListView.items.add("${task.title} - ${task.description} (Due: ${task.dueDate})") }
        }

        val layout = VBox(10.0, titleField, descriptionField, dueDatePicker, priorityComboBox, categoryField, addButton, searchField, searchButton, taskListView)
        primaryStage.scene = Scene(layout, 400.0, 600 .0)
        primaryStage.title = "To-Do List Application"
        primaryStage.show()
        updateTaskList()
    }

    private fun updateTaskList() {
        taskListView.items.clear()
        toDoList.viewTasks().forEach { task -> 
            taskListView.items.add("${task.title} - ${task.description} (Due: ${task.dueDate}) [${task.priority}] [${task.category}]") 
        }
    }
}

fun main() {
    Application.launch(ToDoApp::class.java)
}
