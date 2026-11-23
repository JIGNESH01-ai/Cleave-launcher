#include "internal.h"
#include "frame_counter.h"

// Only context.c's original entry point is renamed by CMake. All callers still use
// this public GLFW API, regardless of their GL renderer (including MobileGlues).
GLFWAPI void cleaveOriginalSwapBuffers(GLFWwindow* handle);
GLFWAPI void glfwSwapBuffers(GLFWwindow* handle) {
    cleaveOriginalSwapBuffers(handle);
    _GLFWwindow* window = (_GLFWwindow*) handle;
    if (_glfw.initialized && window && window->context.client != GLFW_NO_API)
        cleave_record_frame();
}
