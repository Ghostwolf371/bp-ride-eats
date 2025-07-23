<template>
  <div class="space-y-6">
    <!-- Header with Search and Add Order Button -->
    <div
      class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"
    >
      <h2 class="text-3xl font-bold text-white">All Orders</h2>
      <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search orders..."
            class="w-full sm:w-64 px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
          />
          <SearchIcon class="absolute right-3 top-2.5 h-5 w-5 text-gray-400" />
        </div>
        <button
          @click="openAddOrderModal"
          :disabled="isLoading"
          class="bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 flex items-center whitespace-nowrap"
        >
          <PlusIcon class="h-4 w-4 mr-2" />
          Add New Order
        </button>
        <button
          @click="refreshOrders"
          :disabled="isLoading"
          class="bg-gray-700 hover:bg-gray-600 disabled:bg-gray-600 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 flex items-center"
        >
          <RefreshCwIcon :class="['h-4 w-4', isLoading && 'animate-spin']" />
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div
      v-if="isLoading && orders.length === 0"
      class="flex justify-center py-12"
    >
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"
      ></div>
    </div>

    <!-- Countdown Loading Spinner -->
    <div
      v-else-if="!isCountdownsReady && filteredOrders.length > 0"
      class="flex justify-center py-12"
    >
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"
      ></div>
    </div>

    <!-- Orders Grid -->
    <div
      v-else-if="filteredOrders.length > 0"
      class="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
    >
      <div
        v-for="order in filteredOrders"
        :key="order.id"
        class="bg-gray-800 border border-gray-700 rounded-lg p-6 hover:border-green-500 transition-colors duration-200"
      >
        <div class="flex justify-between items-start mb-4">
          <div>
            <h3 class="text-lg font-semibold text-white">
              Order #{{ order.id }}
            </h3>
            <p class="text-green-400 font-medium">
              ${{ (order.total || 0).toFixed(2) }}
            </p>
          </div>
          <span
            :class="[
              'px-3 py-1 text-xs font-medium rounded-full',
              getStatusColor(order.status),
            ]"
          >
            {{ order.status }}
          </span>
        </div>
        <div class="space-y-2 mb-4">
          <div class="flex items-center text-gray-300">
            <UserIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm">{{ order.customerName }}</span>
          </div>
          <div class="flex items-center text-gray-300">
            <MapPinIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm">{{ order.restaurant }}</span>
          </div>
          <div class="flex items-center text-gray-300">
            <ClockIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm">
              <template v-if="order.createdAt && order.preparationTime">
                <span v-if="countdowns[order.id] > 0">
                  {{ formatSecondsToHMS(countdowns[order.id]) }} left
                </span>
                <span v-else class="text-green-400 font-bold">Ready</span>
              </template>
              <template v-else>
                {{ formatMinutesToHMS(order.preparationTime) }} prep time
              </template>
            </span>
          </div>
          <div class="flex items-center text-gray-300">
            <HomeIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm truncate">{{ order.deliveryAddress }}</span>
          </div>
        </div>
        <div class="flex space-x-2">
          <button
            v-if="order.status === 'AVAILABLE' || order.status === 'PREPARING'"
            @click="
              order.status === 'AVAILABLE' ? quickAssignOrder(order) : null
            "
            :disabled="order.status === 'PREPARING' || isAssigning === order.id"
            :title="
              order.status === 'PREPARING'
                ? 'Order is still preparing and cannot be assigned'
                : ''
            "
            class="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 text-sm"
          >
            {{
              order.status === "PREPARING"
                ? "Assign to Biker"
                : isAssigning === order.id
                ? "Assigning..."
                : "Assign to Biker"
            }}
          </button>
          <button
            @click="editOrder(order)"
            :disabled="isUpdating === order.id"
            class="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 text-sm flex items-center justify-center"
          >
            <EditIcon class="h-4 w-4 mr-1" />
            {{ isUpdating === order.id ? "Updating..." : "Edit" }}
          </button>
          <button
            @click="deleteOrder(order.id)"
            :disabled="isDeleting === order.id"
            class="bg-red-600 hover:bg-red-700 disabled:bg-gray-600 text-white font-medium py-2 px-3 rounded-lg transition-colors duration-200"
          >
            <TrashIcon class="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <ShoppingBagIcon class="h-16 w-16 text-gray-600 mx-auto mb-4" />
      <p class="text-gray-400 text-lg font-medium">No orders found</p>
      <p class="text-gray-500 mt-2">
        {{
          searchQuery
            ? "Try adjusting your search criteria"
            : "No available orders at the moment"
        }}
      </p>
    </div>

    <!-- Add/Edit Order Modal -->
    <div
      v-if="showOrderModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
    >
      <div
        class="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700"
      >
        <h3 class="text-xl font-bold text-white mb-4">
          {{ editingOrder ? "Edit Order" : "Add New Order" }}
        </h3>
        <form @submit.prevent="saveOrder" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Customer Name</label
            >
            <input
              v-model="orderForm.customerName"
              type="text"
              required
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Restaurant</label
            >
            <input
              v-model="orderForm.restaurant"
              type="text"
              required
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
            />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1"
                >Preparation Time (min)</label
              >
              <input
                v-model.number="orderForm.preparationTime"
                type="number"
                min="1"
                required
                class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1"
                >Total Amount ($)</label
              >
              <input
                v-model.number="orderForm.total"
                type="number"
                min="0"
                step="0.01"
                required
                class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Delivery Address</label
            >
            <textarea
              v-model="orderForm.deliveryAddress"
              required
              rows="3"
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
            ></textarea>
          </div>
          <div class="flex space-x-3">
            <button
              type="button"
              @click="closeModal"
              class="flex-1 bg-gray-600 hover:bg-gray-700 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="isSaving"
              class="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
            >
              {{ isSaving ? "Saving..." : "Save Order" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <!-- Assignment Modal -->
    <div
      v-if="showAssignmentModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
    >
      <div
        class="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700"
      >
        <h3 class="text-xl font-bold text-white mb-4">Assign Order to Biker</h3>
        <div class="mb-4 p-4 bg-gray-700 rounded-lg">
          <h4 class="font-medium text-green-400 mb-2">Order Details</h4>
          <p class="text-sm text-gray-300">
            Order #{{ selectedOrderForAssignment?.id }}
          </p>
          <p class="text-sm text-gray-300">
            Customer: {{ selectedOrderForAssignment?.customerName }}
          </p>
          <p class="text-sm text-gray-300">
            Total: ${{ (selectedOrderForAssignment?.total || 0).toFixed(2) }}
          </p>
        </div>
        <div class="space-y-3 mb-4 max-h-64 overflow-y-auto">
          <div
            v-for="biker in availableBikers"
            :key="biker.id"
            @click="selectedBikerForAssignment = biker"
            :class="[
              'p-3 border rounded-lg cursor-pointer transition-colors duration-200',
              selectedBikerForAssignment?.id === biker.id
                ? 'border-green-500 bg-green-900/20'
                : 'border-gray-600 hover:border-green-400 hover:bg-gray-700',
            ]"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <div
                  class="h-8 w-8 bg-green-600 rounded-full flex items-center justify-center mr-3"
                >
                  <span class="text-white font-bold text-sm">{{
                    (biker.name || "B").charAt(0).toUpperCase()
                  }}</span>
                </div>
                <div>
                  <p class="font-medium text-white">{{ biker.name }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="flex justify-end space-x-3 pt-2">
          <button
            @click="closeAssignmentModal"
            class="bg-gray-600 hover:bg-gray-700 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
          >
            Cancel
          </button>
          <button
            @click="assignOrderToBiker"
            :disabled="isAssigning || !selectedBikerForAssignment"
            class="bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
          >
            {{ isAssigning ? "Assigning..." : "Assign" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject, watch } from "vue";
import {
  SearchIcon,
  UserIcon,
  MapPinIcon,
  ClockIcon,
  HomeIcon,
  ShoppingBagIcon,
  RefreshCwIcon,
  EditIcon,
  TrashIcon,
  PlusIcon,
} from "lucide-vue-next";
import { ordersApi, bikersApi } from "../services/api.js";

const showError = inject("showError");
const showSuccess = inject("showSuccess");

const orders = ref([]);
const searchQuery = ref("");
const statusFilter = ref("");
const isLoading = ref(false);
const isAssigning = ref(null);
const isUpdating = ref(null);
const isDeleting = ref(null);

const showOrderModal = ref(false);
const editingOrder = ref(null);
const isSaving = ref(false);
const orderForm = ref({
  customerName: "",
  restaurant: "",
  preparationTime: 20,
  total: 0,
  deliveryAddress: "",
});

const showAssignmentModal = ref(false);
const selectedOrderForAssignment = ref(null);
const availableBikers = ref([]);
const selectedBikerForAssignment = ref(null);

const countdowns = ref({});
let countdownInterval = null;
const isCountdownsReady = ref(false);

const startCountdowns = () => {
  if (countdownInterval) clearInterval(countdownInterval);
  let firstUpdate = true;
  countdownInterval = setInterval(() => {
    const now = Date.now();
    orders.value.forEach((order) => {
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

const fetchOrders = async () => {
  isLoading.value = true;
  isCountdownsReady.value = false;
  try {
    const { data, error } = await ordersApi.getAllOrders();
    if (error) {
      showError("Failed to fetch orders: " + error);
    } else {
      orders.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch orders");
  } finally {
    isLoading.value = false;
  }
};

const refreshOrders = async () => {
  await fetchOrders();
  showSuccess("Orders refreshed successfully");
};

const filteredOrders = computed(() => {
  let filtered = orders.value;
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (order) =>
        order.id?.toLowerCase().includes(query) ||
        order.customerName?.toLowerCase().includes(query) ||
        order.restaurant?.toLowerCase().includes(query) ||
        order.deliveryAddress?.toLowerCase().includes(query)
    );
  }
  if (statusFilter.value) {
    filtered = filtered.filter((order) => order.status === statusFilter.value);
  }
  return filtered;
});

const getStatusColor = (status) => {
  switch (status) {
    case "AVAILABLE":
      return "bg-yellow-900 text-yellow-300";
    case "PREPARING":
      return "bg-orange-900 text-orange-300";
    case "COMPLETED":
      return "bg-green-900 text-green-300";
    default:
      return "bg-gray-900 text-gray-300";
  }
};

function openAddOrderModal() {
  editingOrder.value = null;
  orderForm.value = {
    customerName: "",
    restaurant: "",
    preparationTime: 20,
    total: 0,
    deliveryAddress: "",
  };
  showOrderModal.value = true;
}
function closeModal() {
  showOrderModal.value = false;
  editingOrder.value = null;
}
async function saveOrder() {
  isSaving.value = true;
  try {
    let result;
    if (editingOrder.value) {
      result = await ordersApi.updateOrder(
        editingOrder.value.id,
        orderForm.value
      );
    } else {
      result = await ordersApi.createOrder(orderForm.value);
    }
    if (result.error) {
      showError(result.error);
    } else {
      await fetchOrders();
      showSuccess(
        editingOrder.value
          ? "Order updated successfully"
          : "Order added successfully"
      );
      closeModal();
    }
  } catch (err) {
    showError("Failed to save order");
  } finally {
    isSaving.value = false;
  }
}

function editOrder(order) {
  editingOrder.value = order;
  orderForm.value = {
    customerName: order.customerName || "",
    restaurant: order.restaurant || "",
    preparationTime: order.preparationTime || 20,
    total: order.total || 0,
    deliveryAddress: order.deliveryAddress || "",
  };
  showOrderModal.value = true;
}

async function quickAssignOrder(order) {
  selectedOrderForAssignment.value = order;
  selectedBikerForAssignment.value = null;
  showAssignmentModal.value = true;
  await fetchAvailableBikers();
}

async function fetchAvailableBikers() {
  try {
    const { data, error } = await bikersApi.getAvailableBikers();
    if (error) {
      showError("Failed to fetch bikers: " + error);
      availableBikers.value = [];
    } else {
      availableBikers.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch bikers");
    availableBikers.value = [];
  }
}

function closeAssignmentModal() {
  showAssignmentModal.value = false;
  selectedOrderForAssignment.value = null;
  selectedBikerForAssignment.value = null;
}

async function assignOrderToBiker() {
  if (!selectedOrderForAssignment.value || !selectedBikerForAssignment.value)
    return;
  isAssigning.value = selectedOrderForAssignment.value.id;
  try {
    const { error } = await ordersApi.assignOrderToBiker(
      selectedOrderForAssignment.value.id,
      selectedBikerForAssignment.value.id
    );
    if (error) {
      showError("Failed to assign order: " + error);
    } else {
      await fetchOrders();
      showSuccess("Order assigned successfully");
      closeAssignmentModal();
    }
  } catch (err) {
    showError("Failed to assign order");
  } finally {
    isAssigning.value = null;
  }
}

async function deleteOrder(orderId) {
  if (
    confirm(
      "Are you sure you want to delete this order? This action cannot be undone."
    )
  ) {
    isDeleting.value = orderId;
    try {
      const { error } = await ordersApi.deleteOrder(orderId);
      if (error) {
        showError("Failed to delete order: " + error);
      } else {
        orders.value = orders.value.filter((o) => o.id !== orderId);
        showSuccess("Order deleted successfully");
      }
    } catch (err) {
      showError("Failed to delete order");
    } finally {
      isDeleting.value = null;
    }
  }
}

const formatMinutesToHMS = (minutes) => {
  if (typeof minutes !== "number" || isNaN(minutes)) return "00:00:00";
  const totalSeconds = Math.floor(minutes * 60);
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  return [h, m, s].map((v) => v.toString().padStart(2, "0")).join(":");
};

onMounted(() => {
  fetchOrders();
  startCountdowns();
});

// Also restart countdowns when orders change
watch(orders, () => {
  startCountdowns();
});
</script>
