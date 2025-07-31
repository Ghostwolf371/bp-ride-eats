<template>
  <div class="space-y-6">
    <!-- Header with Search and Add Button -->
    <div
      class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4"
    >
      <h2 class="text-3xl font-bold text-white">Manage Bikers</h2>
      <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search bikers..."
            class="w-full sm:w-64 px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
          />
          <div class="absolute right-3 top-2.5">
            <RefreshCwIcon
              v-if="isSearching"
              class="h-5 w-5 text-green-400 animate-spin"
            />
            <SearchIcon v-else class="h-5 w-5 text-gray-400" />
          </div>
        </div>
        <button
          @click="openAddBikerModal"
          :disabled="isLoading"
          class="bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 flex items-center whitespace-nowrap"
        >
          <PlusIcon class="h-4 w-4 mr-2" />
          Add Biker
        </button>
        <button
          @click="refreshBikers"
          :disabled="isLoading"
          class="bg-gray-700 hover:bg-gray-600 disabled:bg-gray-600 text-white px-4 py-2 rounded-lg font-medium transition-colors duration-200 flex items-center"
        >
          <RefreshCwIcon :class="['h-4 w-4', isLoading && 'animate-spin']" />
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div
      v-if="isLoading && bikers.length === 0"
      class="flex justify-center py-12"
    >
      <div
        class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"
      ></div>
    </div>

    <!-- Bikers Grid -->
    <div
      v-else-if="filteredBikers.length > 0"
      class="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
    >
      <div
        v-for="biker in filteredBikers"
        :key="biker.id"
        class="bg-gray-800 border border-gray-700 rounded-lg p-6 hover:border-green-500 transition-colors duration-200"
      >
        <div class="flex justify-between items-start mb-4">
          <div class="flex items-center">
            <div
              class="h-12 w-12 bg-green-600 rounded-full flex items-center justify-center"
            >
              <span class="text-white font-bold text-lg">{{
                (biker.name || "B").charAt(0).toUpperCase()
              }}</span>
            </div>
            <div class="ml-3">
              <h3 class="text-lg font-semibold text-white">{{ biker.name }}</h3>
              <p class="text-sm text-gray-400">ID: {{ biker.id }}</p>
            </div>
          </div>
        </div>

        <div class="space-y-2 mb-4">
          <div class="flex items-center text-gray-300">
            <PhoneIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm">{{ biker.phone }}</span>
          </div>
          <div class="flex items-center text-gray-300">
            <MailIcon class="h-4 w-4 mr-2 text-green-400" />
            <span class="text-sm">{{ biker.email }}</span>
          </div>
        </div>

        <div class="flex space-x-2">
          <button
            @click="editBiker(biker)"
            :disabled="isUpdating === biker.id"
            class="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white font-medium py-2 px-4 rounded-lg transition-colors duration-200 flex items-center justify-center"
          >
            <EditIcon class="h-4 w-4 mr-1" />
            {{ isUpdating === biker.id ? "Updating..." : "Edit" }}
          </button>
          <button
            @click="deleteBiker(biker.id, biker.name)"
            :disabled="isDeleting === biker.id"
            class="flex-1 bg-gray-700 hover:bg-red-600 disabled:bg-gray-600 border border-gray-600 hover:border-red-500 text-gray-300 hover:text-white font-medium py-2 px-4 rounded-lg transition-all duration-200 flex items-center justify-center"
          >
            <TrashIcon class="h-4 w-4 mr-1" />
            {{ isDeleting === biker.id ? "Removing..." : "Remove" }}
          </button>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <BikeIcon class="h-16 w-16 text-gray-600 mx-auto mb-4" />
      <p class="text-gray-400 text-lg font-medium">No bikers found</p>
      <p class="text-gray-500 mt-2">
        {{
          searchQuery && /^\d+$/.test(searchQuery.trim())
            ? `No biker found with ID ${searchQuery}`
            : searchQuery
            ? "No bikers match your search criteria"
            : "Add your first biker to get started"
        }}
      </p>
    </div>

    <!-- Add/Edit Biker Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50"
    >
      <div
        class="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700"
      >
        <h3 class="text-xl font-bold text-white mb-4">
          {{ editingBiker ? "Edit Biker" : "Add New Biker" }}
        </h3>

        <form @submit.prevent="saveBiker" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Name *</label
            >
            <input
              v-model="bikerForm.name"
              type="text"
              required
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Phone *</label
            >
            <input
              v-model="bikerForm.phone"
              type="tel"
              required
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1"
              >Email *</label
            >
            <input
              v-model="bikerForm.email"
              type="email"
              required
              class="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>

          <div class="flex space-x-3 pt-4">
            <button
              type="button"
              @click="closeModal"
              :disabled="isSaving"
              class="flex-1 bg-gray-600 hover:bg-gray-700 disabled:bg-gray-700 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="isSaving"
              class="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-2 px-4 rounded-lg font-medium transition-colors duration-200"
            >
              {{
                isSaving
                  ? editingBiker
                    ? "Updating..."
                    : "Adding..."
                  : editingBiker
                  ? "Update"
                  : "Add"
              }}
              Biker
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, watch, computed } from "vue";
import {
  SearchIcon,
  PlusIcon,
  PhoneIcon,
  MailIcon,
  EditIcon,
  TrashIcon,
  BikeIcon,
  RefreshCwIcon,
} from "lucide-vue-next";
import { bikersApi } from "../services/api.js";

const showError = inject("showError");
const showSuccess = inject("showSuccess");

// Reactive state
const bikers = ref([]);
const searchQuery = ref("");
const showModal = ref(false);
const editingBiker = ref(null);
const isLoading = ref(false);
const isSaving = ref(false);
const isUpdating = ref(null);
const isDeleting = ref(null);
const isSearching = ref(false);
const binarySearchResult = ref(null);

const bikerForm = ref({
  name: "",
  phone: "",
  email: "",
});

// Computed property for filtered bikers
const filteredBikers = computed(() => {
  // If searching by numeric ID but no result yet, return empty
  if (searchQuery.value && /^\d+$/.test(searchQuery.value.trim())) {
    return binarySearchResult.value ? [binarySearchResult.value] : [];
  }
  // For non-ID searches, return all bikers (no client-side filtering)
  return bikers.value;
});

// Methods
// Binary search function for bikers
const performBikerBinarySearch = async (bikerId) => {
  isSearching.value = true;
  try {
    const { data, error } = await bikersApi.searchBikerByIdBinary(bikerId);
    if (error) {
      binarySearchResult.value = null;
    }
    binarySearchResult.value = data;

    if (data) {
      showSuccess(`Biker #${bikerId} found using binary search!`);
    } else {
      showError(`Biker #${bikerId} not found`);
    }
  } catch (err) {
    showError("Failed to search biker");
    binarySearchResult.value = null;
  } finally {
    isSearching.value = false;
  }
};

const fetchBikers = async () => {
  isLoading.value = true;
  try {
    const { data, error } = await bikersApi.getAllBikers();
    if (error) {
      showError("Failed to fetch bikers: " + error);
    } else {
      bikers.value = data || [];
    }
  } catch (err) {
    showError("Failed to fetch bikers");
  } finally {
    isLoading.value = false;
  }
};

const refreshBikers = async () => {
  await fetchBikers();
  showSuccess("Bikers refreshed successfully");
};

const openAddBikerModal = () => {
  editingBiker.value = null;
  bikerForm.value = {
    name: "",
    phone: "",
    email: "",
  };
  showModal.value = true;
};

const editBiker = (biker) => {
  editingBiker.value = biker;
  bikerForm.value = {
    name: biker.name || "",
    phone: biker.phone || "",
    email: biker.email || "",
  };
  showModal.value = true;
};

const saveBiker = async () => {
  isSaving.value = true;
  try {
    if (editingBiker.value) {
      // Update existing biker
      isUpdating.value = editingBiker.value.id;
      const { data, error } = await bikersApi.updateBiker(
        editingBiker.value.id,
        bikerForm.value
      );
      if (error) {
        showError("Failed to update biker: " + error);
      } else {
        const index = bikers.value.findIndex(
          (b) => b.id === editingBiker.value.id
        );
        if (index > -1) {
          bikers.value[index] = { ...bikers.value[index], ...data };
        }
        showSuccess(`Biker ${bikerForm.value.name} updated successfully`);
        closeModal();
      }
    } else {
      // Add new biker
      const { data, error } = await bikersApi.createBiker(bikerForm.value);
      if (error) {
        showError("Failed to create biker: " + error);
      } else {
        bikers.value.push(data);
        showSuccess(`Biker ${bikerForm.value.name} added successfully`);
        closeModal();
      }
    }
  } catch (err) {
    showError("Failed to save biker");
  } finally {
    isSaving.value = false;
    isUpdating.value = null;
  }
};

const deleteBiker = async (bikerId, bikerName) => {
  if (
    confirm(
      `Are you sure you want to remove ${bikerName}? This action cannot be undone.`
    )
  ) {
    isDeleting.value = bikerId;
    try {
      const { error } = await bikersApi.deleteBiker(bikerId);
      if (error) {
        showError("Failed to delete biker: " + error);
      } else {
        const index = bikers.value.findIndex((b) => b.id === bikerId);
        if (index > -1) {
          bikers.value.splice(index, 1);
        }
        showSuccess(`Biker ${bikerName} removed successfully`);
      }
    } catch (err) {
      showError("Failed to delete biker");
    } finally {
      isDeleting.value = null;
    }
  }
};

const closeModal = () => {
  showModal.value = false;
  editingBiker.value = null;
  bikerForm.value = {
    name: "",
    phone: "",
    email: "",
  };
};

// Watch for search query changes to trigger binary search
watch(searchQuery, (newQuery) => {
  const trimmedQuery = newQuery.trim();

  // Clear binary search result if search is empty
  if (!trimmedQuery) {
    binarySearchResult.value = null;
    return;
  }

  // Only perform binary search for exact numeric IDs (no partial matches)
  if (/^\d+$/.test(trimmedQuery)) {
    // Only search if it's a complete ID (not partial)
    performBikerBinarySearch(trimmedQuery);
  } else {
    // For non-numeric searches, clear binary search result
    binarySearchResult.value = null;
  }
});

// Lifecycle
onMounted(() => {
  fetchBikers();
});
</script>
