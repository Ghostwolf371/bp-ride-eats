// API Base URL - Update this to match your Spring Boot backend
const API_BASE_URL = "http://localhost:8080/api";

// Generic API request function with enhanced error handling
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const config = {
    headers: {
      "Content-Type": "application/json",
      // Add authentication headers here if needed
      // "Authorization": `Bearer ${getAuthToken()}`,
      ...options.headers,
    },
    ...options,
  };

  try {
    const response = await fetch(url, config);

    // Handle different response types
    if (response.status === 204) {
      // No content response (successful DELETE)
      return { data: null, error: null };
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.message || `HTTP error! status: ${response.status}`
      );
    }

    const data = await response.json();
    return { data, error: null };
  } catch (error) {
    console.error("API request failed:", error);
    return { data: null, error: error.message };
  }
}

// Orders API
export const ordersApi = {
  // Get all assignable orders
  getAssignableOrders: () => apiRequest("/orders/assignable"),

  // Get all orders
  getAllOrders: () => apiRequest("/orders"),

  // Create new order
  createOrder: (orderData) =>
    apiRequest("/orders", {
      method: "POST",
      body: JSON.stringify(orderData),
    }),

  // Update order
  updateOrder: (orderId, orderData) =>
    apiRequest(`/orders/${orderId}`, {
      method: "PUT",
      body: JSON.stringify(orderData),
    }),

  // Delete order
  deleteOrder: (orderId) =>
    apiRequest(`/orders/${orderId}`, {
      method: "DELETE",
    }),

  // Assign order to biker (moves from available to assigned/in-progress)
  assignOrderToBiker: (orderId, bikerId) =>
    apiRequest(`/orders/${orderId}/assign`, {
      method: "PUT",
      body: JSON.stringify({ bikerId }),
    }),

  // Search order by ID using backend binary search
  searchOrderByIdBinary: (orderId) => apiRequest(`/orders/binary/${orderId}`),

  // Search assignable order by ID using backend binary search
  searchAssignableOrderByIdBinary: (orderId) =>
    apiRequest(`/orders/assignable/binary/${orderId}`),
};

// Bikers API
export const bikersApi = {
  // Get all bikers
  getAllBikers: () => apiRequest("/bikers"),

  // Search biker by ID using backend binary search
  searchBikerByIdBinary: (bikerId) => apiRequest(`/bikers/binary/${bikerId}`),

  // Create new biker
  createBiker: (bikerData) =>
    apiRequest("/bikers", {
      method: "POST",
      body: JSON.stringify(bikerData),
    }),

  // Update biker
  updateBiker: (bikerId, bikerData) =>
    apiRequest(`/bikers/${bikerId}`, {
      method: "PUT",
      body: JSON.stringify(bikerData),
    }),

  // Delete biker
  deleteBiker: (bikerId) =>
    apiRequest(`/bikers/${bikerId}`, {
      method: "DELETE",
    }),
};

// Assignments API
export const assignmentsApi = {
  // Get assignment history
  getAssignmentHistory: () => apiRequest("/assignments"),
};
