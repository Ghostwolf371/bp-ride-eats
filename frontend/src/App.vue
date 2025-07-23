<template>
  <div class="min-h-screen bg-gray-900">
    <!-- Header -->
    <header class="bg-black shadow-lg border-b border-green-500">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-4">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <h1 class="text-3xl font-bold text-green-400">Ride Eats</h1>
              <p class="text-sm text-gray-300">Management Dashboard</p>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Navigation -->
    <nav class="bg-gray-800 border-b border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex space-x-8">
          <button
            v-for="page in pages"
            :key="page.id"
            @click="currentPage = page.id"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200',
              currentPage === page.id
                ? 'border-green-400 text-green-400'
                : 'border-transparent text-gray-300 hover:text-green-300 hover:border-green-300',
            ]"
          >
            {{ page.name }}
          </button>
        </div>
      </div>
    </nav>

    <!-- Page Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Orders v-if="currentPage === 'orders'" />
      <ManageBikers v-if="currentPage === 'manage-bikers'" />
      <AssignOrders v-if="currentPage === 'assign-orders'" />
    </main>

    <!-- Global Error Toast -->
    <div
      v-if="globalError"
      class="fixed bottom-4 right-4 bg-red-600 text-white px-6 py-3 rounded-lg shadow-lg z-50 max-w-md"
    >
      <div class="flex items-start">
        <div class="flex-1">
          <p class="font-medium">Error</p>
          <p class="text-sm">{{ globalError }}</p>
        </div>
        <button
          @click="globalError = null"
          class="ml-4 text-white hover:text-gray-200 text-xl leading-none"
        >
          ×
        </button>
      </div>
    </div>

    <!-- Global Success Toast -->
    <div
      v-if="globalSuccess"
      class="fixed bottom-4 right-4 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg z-50 max-w-md"
    >
      <div class="flex items-start">
        <div class="flex-1">
          <p class="font-medium">Success</p>
          <p class="text-sm">{{ globalSuccess }}</p>
        </div>
        <button
          @click="globalSuccess = null"
          class="ml-4 text-white hover:text-gray-200 text-xl leading-none"
        >
          ×
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, provide } from "vue";
import Orders from "./components/Orders.vue";
import AssignOrders from "./components/AssignOrders.vue";
import ManageBikers from "./components/ManageBikers.vue";

const currentPage = ref("orders");
const globalError = ref(null);
const globalSuccess = ref(null);

const pages = [
  { id: "orders", name: "Orders" },
  { id: "manage-bikers", name: "Manage Bikers" },
  { id: "assign-orders", name: "Assign Orders" },
];

// Provide global notification handlers
provide("showError", (message) => {
  globalError.value = message;
  setTimeout(() => {
    globalError.value = null;
  }, 5000);
});

provide("showSuccess", (message) => {
  globalSuccess.value = message;
  setTimeout(() => {
    globalSuccess.value = null;
  }, 3000);
});
</script>
