<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <h2 class="text-3xl font-bold text-white">Assign Orders to Bikers</h2>
      <div class="flex gap-2">
        <button
          @click="refreshData"
          :disabled="isLoading"
          class="bg-gray-700 hover:bg-gray-600 disabled:bg-gray-600 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 flex items-center"
        >
          <RefreshCwIcon
            :class="['h-4 w-4 mr-2', isLoading && 'animate-spin']"
          />
          Refresh
        </button>
      </div>
    </div>

    <!-- Search Bars -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="relative">
        <input
          v-model="orderSearchQuery"
          type="text"
          placeholder="Search orders..."
          class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
        />
        <SearchIcon class="absolute right-3 top-2.5 h-5 w-5 text-gray-400" />
      </div>
      <div class="relative">
        <input
          v-model="bikerSearchQuery"
          type="text"
          placeholder="Search bikers..."
          class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
        />
        <SearchIcon class="absolute right-3 top-2.5 h-5 w-5 text-gray-400" />
      </div>
    </div>

    <!-- Loading State -->
    <div
      v-if="isLoading || (!isCountdownsReady && assignableOrders.length > 0)"
      class="flex justify-center py-12"
    >
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"
      ></div>
    </div>

    <!-- Assignment Interface -->
    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Available Orders -->
      <div class="bg-gray-800 border border-gray-700 rounded-lg">
        <div class="p-4 border-b border-gray-700">
          <h3 class="text-lg font-semibold text-white flex items-center">
            <ShoppingBagIcon class="h-5 w-5 mr-2 text-green-400" />
            Available Orders ({{ assignableOrders.length }})
          </h3>
        </div>
        <div class="p-4 space-y-3 max-h-96 overflow-y-auto">
          <div v-if="assignableOrders.length === 0" class="text-center py-8">
            <ShoppingBagIcon class="h-12 w-12 text-gray-600 mx-auto mb-2" />
            <p class="text-gray-400">No available orders</p>
          </div>
          <div
            v-for="order in assignableOrders"
            :key="order.id"
            @click="countdowns[order.id] === 0 ? selectOrder(order) : null"
            :class="[
              'p-4 border rounded-lg transition-all duration-200',
              countdowns[order.id] > 0
                ? [
                    'cursor-not-allowed opacity-60',
                    selectedOrder?.id === order.id
                      ? 'border-red-500 bg-red-900/20'
                      : 'border-gray-600 hover:border-red-400 hover:bg-gray-700',
                  ]
                : [
                    'cursor-pointer',
                    selectedOrder?.id === order.id
                      ? 'border-green-500 bg-green-900/20'
                      : 'border-gray-600 hover:border-green-400 hover:bg-gray-700',
                  ],
            ]"
            :title="countdowns[order.id] > 0 ? 'Order is not ready yet' : ''"
          >
            <div class="flex justify-between items-start mb-2">
              <div>
                <h4 class="font-medium text-white">Order #{{ order.id }}</h4>
                <p class="text-sm text-gray-400">{{ order.customerName }}</p>
              </div>
              <div class="text-right">
                <p class="font-medium text-green-400">
                  ${{ (order.total || 0).toFixed(2) }}
                </p>
                <div class="flex items-center gap-2 mt-2 justify-end">
                  <span
                    class="bg-green-700 text-white text-xs font-mono px-2 py-1 rounded flex items-center gap-1"
                  >
                    <ClockIcon class="h-4 w-4" />
                    <template v-if="order.createdAt && order.preparationTime">
                      <span v-if="countdowns[order.id] > 0">
                        {{ formatSecondsToHMS(countdowns[order.id]) }} left
                      </span>
                      <span v-else class="text-green-400 font-bold">Ready</span>
                    </template>
                    <template v-else>
                      {{ formatMinutesToHMS(order.preparationTime) }}
                    </template>
                  </span>
                </div>
              </div>
            </div>
            <div class="flex items-center text-sm text-gray-300">
              <MapPinIcon class="h-4 w-4 mr-1 text-green-400" />
              <span>{{ order.restaurant }}</span>
            </div>
            <div class="flex items-center text-sm text-gray-300 mt-1">
              <HomeIcon class="h-4 w-4 mr-1 text-green-400" />
              <span class="truncate">{{ order.deliveryAddress }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Available Bikers -->
      <div class="bg-gray-800 border border-gray-700 rounded-lg">
        <div class="p-4 border-b border-gray-700">
          <h3 class="text-lg font-semibold text-white flex items-center">
            <BikeIcon class="h-5 w-5 mr-2 text-green-400" />
            Available Bikers ({{ availableBikers.length }})
          </h3>
        </div>
        <div class="p-4 space-y-3 max-h-96 overflow-y-auto">
          <div v-if="availableBikers.length === 0" class="text-center py-8">
            <BikeIcon class="h-12 w-12 text-gray-600 mx-auto mb-2" />
            <p class="text-gray-400">No available bikers</p>
          </div>
          <div
            v-for="biker in availableBikers"
            :key="biker.id"
            @click="selectBiker(biker)"
            :class="[
              'p-4 border rounded-lg cursor-pointer transition-all duration-200',
              selectedBiker?.id === biker.id
                ? 'border-green-500 bg-green-900/20'
                : 'border-gray-600 hover:border-green-400 hover:bg-gray-700',
            ]"
          >
            <div class="flex items-center mb-2">
              <div
                class="h-10 w-10 bg-green-600 rounded-full flex items-center justify-center mr-3"
              >
                <span class="text-white font-bold">{{
                  (biker.name || "B").charAt(0).toUpperCase()
                }}</span>
              </div>
              <div class="flex-1">
                <h4 class="font-medium text-white">{{ biker.name }}</h4>
              </div>
              <div class="text-right">
                <div class="flex items-center text-sm text-gray-300">
                  <PhoneIcon class="h-4 w-4 mr-1 text-green-400" />
                  <span>{{ biker.phone }}</span>
                </div>
                <div class="flex items-center text-sm text-gray-300">
                  <MailIcon class="h-4 w-4 mr-1 text-green-400" />
                  <span>{{ biker.email }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Assignment Summary -->
    <div
      v-if="selectedOrder && selectedBiker"
      class="bg-gray-800 border border-green-500 rounded-lg p-6"
    >
      <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
        <CheckCircleIcon class="h-5 w-5 mr-2 text-green-400" />
        Assignment Summary
      </h3>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        <div class="bg-gray-700 rounded-lg p-4">
          <h4 class="font-medium text-green-400 mb-2">Order Details</h4>
          <div class="space-y-1 text-sm text-gray-300">
            <p>
              <span class="text-gray-400">Order ID:</span> #{{
                selectedOrder.id
              }}
            </p>
            <p>
              <span class="text-gray-400">Customer:</span>
              {{ selectedOrder.customerName }}
            </p>
            <p>
              <span class="text-gray-400">Restaurant:</span>
              {{ selectedOrder.restaurant }}
            </p>
            <p>
              <span class="text-gray-400">Total:</span> ${{
                (selectedOrder.total || 0).toFixed(2)
              }}
            </p>
            <p>
              <span class="text-gray-400">Prep Time:</span>
              {{ selectedOrder.preparationTime }} min
            </p>
            <p>
              <span class="text-gray-400">Address:</span>
              {{ selectedOrder.deliveryAddress }}
            </p>
          </div>
        </div>

        <div class="bg-gray-700 rounded-lg p-4">
          <h4 class="font-medium text-green-400 mb-2">Biker Details</h4>
          <div class="space-y-1 text-sm text-gray-300">
            <p>
              <span class="text-gray-400">Name:</span> {{ selectedBiker.name }}
            </p>
            <p>
              <span class="text-gray-400">Phone:</span>
              {{ selectedBiker.phone }}
            </p>
            <p>
              <span class="text-gray-400">Email:</span>
              {{ selectedBiker.email }}
            </p>
          </div>
        </div>
      </div>

      <div class="flex space-x-4">
        <button
          @click="clearSelection"
          :disabled="isAssigning"
          class="flex-1 bg-gray-600 hover:bg-gray-700 disabled:bg-gray-700 text-white py-3 px-4 rounded-lg font-medium transition-colors duration-200"
        >
          Clear Selection
        </button>
        <button
          @click="confirmAssignment"
          :disabled="isAssigning"
          class="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-3 px-4 rounded-lg font-medium transition-colors duration-200 flex items-center justify-center"
        >
          <CheckIcon class="h-4 w-4 mr-2" />
          {{ isAssigning ? "Assigning..." : "Confirm Assignment" }}
        </button>
      </div>
    </div>

    <!-- Assignment History -->
    <div class="bg-gray-800 border border-gray-700 rounded-lg">
      <div class="p-4 border-b border-gray-700">
        <h3 class="text-lg font-semibold text-white flex items-center">
          <ClockIcon class="h-5 w-5 mr-2 text-green-400" />
          Recent Assignments
        </h3>
      </div>
      <div class="p-4">
        <div v-if="assignmentHistory.length === 0" class="text-center py-8">
          <ClockIcon class="h-12 w-12 text-gray-600 mx-auto mb-2" />
          <p class="text-gray-400">No recent assignments</p>
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="assignment in assignmentHistory"
            :key="assignment.id"
            class="flex items-center justify-between p-3 bg-gray-700 rounded-lg"
          >
            <div class="flex items-center">
              <div
                class="h-8 w-8 bg-green-600 rounded-full flex items-center justify-center mr-3"
              >
                <CheckIcon class="h-4 w-4 text-white" />
              </div>
              <div>
                <p class="text-sm font-medium text-white">
                  Order #{{ assignment.orderId }} → {{ assignment.bikerName }}
                </p>
                <p class="text-xs text-gray-400">
                  {{ formatDate(assignment.assignedAt) }}
                </p>
              </div>
            </div>
            <span
              :class="[
                'px-2 py-1 text-xs font-medium rounded-full',
                assignment.status === 'COMPLETED'
                  ? 'bg-green-900 text-green-300'
                  : 'bg-yellow-900 text-yellow-300',
              ]"
            >
              {{ assignment.status }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject, watch } from "vue";
import {
  SearchIcon,
  ShoppingBagIcon,
  BikeIcon,
  MapPinIcon,
  HomeIcon,
  PhoneIcon,
  CheckCircleIcon,
  CheckIcon,
  ClockIcon,
  RefreshCwIcon,
} from "lucide-vue-next";
import { ordersApi, bikersApi, assignmentsApi } from "../services/api.js";
import { MailIcon } from "lucide-vue-next";

const showError = inject("showError");
const showSuccess = inject("showSuccess");

// Reactive state
const assignableOrders = ref([]);
const availableBikers = ref([]);
const assignmentHistory = ref([]);
const orderSearchQuery = ref("");
const bikerSearchQuery = ref("");
const selectedOrder = ref(null);
const selectedBiker = ref(null);
const isLoading = ref(false);
const isAssigning = ref(false);

const countdowns = ref({});
const isCountdownsReady = ref(false);
let countdownInterval = null;

const startCountdowns = () => {
  if (countdownInterval) clearInterval(countdownInterval);
  let firstUpdate = true;
  countdownInterval = setInterval(() => {
    const now = Date.now();
    assignableOrders.value.forEach((order) => {
      if (!order.createdAt || !order.preparationTime) return;
      const readyAt =
        new Date(order.createdAt).getTime() + order.preparationTime * 60 * 1000;
      const diff = Math.max(0, Math.floor((readyAt - now) / 1000));
      countdowns.value[order.id] = diff;
    });
    if (firstUpdate) {
      isCountdownsReady.value = true;
      firstUpdate = false;
    }
  }, 1000);
};

const formatSecondsToHMS = (seconds) => {
  if (seconds <= 0) return "00:00:00";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  return [h, m, s].map((v) => v.toString().padStart(2, "0")).join(":");
};

// Methods
const fetchAssignableOrders = async () => {
  try {
    const { data, error } = await ordersApi.getAssignableOrders();
    if (error) {
      showError("Failed to fetch available orders: " + error);
    } else {
      assignableOrders.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch available orders");
  }
};

const fetchAvailableBikers = async () => {
  try {
    const { data, error } = await bikersApi.getAvailableBikers();
    if (error) {
      showError("Failed to fetch available bikers: " + error);
    } else {
      availableBikers.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch available bikers");
  }
};

const fetchAssignmentHistory = async () => {
  try {
    const { data, error } = await assignmentsApi.getAssignmentHistory();
    if (error) {
      showError("Failed to fetch assignment history: " + error);
    } else {
      assignmentHistory.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch assignment history");
  }
};

const refreshData = async () => {
  isLoading.value = true;
  try {
    await Promise.all([
      fetchAssignableOrders(),
      fetchAvailableBikers(),
      fetchAssignmentHistory(),
    ]);
    showSuccess("Data refreshed successfully");
  } finally {
    isLoading.value = false;
  }
};

const selectOrder = (order) => {
  selectedOrder.value = order;
};

const selectBiker = (biker) => {
  selectedBiker.value = biker;
};

const clearSelection = () => {
  selectedOrder.value = null;
  selectedBiker.value = null;
};

const confirmAssignment = async () => {
  if (!selectedOrder.value || !selectedBiker.value) return;

  isAssigning.value = true;
  try {
    const { error } = await ordersApi.assignOrderToBiker(
      selectedOrder.value.id,
      selectedBiker.value.id
    );

    if (error) {
      showError("Failed to assign order: " + error);
    } else {
      showSuccess(
        `Order ${selectedOrder.value.id} successfully assigned to ${selectedBiker.value.name}!`
      );
      await fetchAssignmentHistory();
      await fetchAssignableOrders();
      clearSelection();
    }
  } catch (err) {
    showError("Failed to assign order");
  } finally {
    isAssigning.value = false;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "N/A";
  try {
    return new Date(dateString).toLocaleString();
  } catch {
    return dateString;
  }
};

const formatMinutesToHMS = (minutes) => {
  if (typeof minutes !== "number" || isNaN(minutes)) return "00:00:00";
  const totalSeconds = Math.floor(minutes * 60);
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  return [h, m, s].map((v) => v.toString().padStart(2, "0")).join(":");
};

// Lifecycle
onMounted(() => {
  refreshData();
  startCountdowns();
});

watch(assignableOrders, () => {
  isCountdownsReady.value = false;
  startCountdowns();
});
</script>
