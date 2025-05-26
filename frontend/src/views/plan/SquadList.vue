<template>
  <!-- Header -->
        <div class="squad-header rounded-3 p-4 mb-4">
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center">
                    <i class="bi bi-shield-fill-check text-primary me-3" style="font-size: 2rem;"></i>
                    <h1 class="text-neon mb-0 fw-bold">My Squad Rooms</h1>
                </div>
                <button @click="showModal = true" class="btn gaming-btn text-white px-4 py-2 fw-semibold">
                    <i class="bi bi-plus-lg me-2"></i>Create Squad
                </button>
            </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="d-flex justify-content-center align-items-center" style="height: 300px;">
            <div class="text-center">
                <div class="loading-spinner rounded-circle mx-auto mb-3" style="width: 3rem; height: 3rem;"></div>
                <h5 class="text-white">Loading Squads...</h5>
            </div>
        </div>

        <!-- Squad Grid -->
        <div v-else class="row g-4">
            <div v-for="squad in squads" :key="squad.squadId" class="col-lg-4 col-md-6">
                <div class="gaming-card rounded-3 p-4 h-100 cursor-pointer" @click="goToSquad(squad.squadId)">
                    <!-- Card Header -->
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-shield-fill text-primary me-2"></i>
                            <h5 class="text-white mb-0 fw-bold">{{ squad.squadName }}</h5>
                        </div>
                        <i v-if="squad.usernameCreated === userStore.user.name" class="bi bi-crown-fill crown-icon"></i>
                    </div>

                    <!-- Squad Info -->
                    <div class="mb-3">
                        <div class="d-flex align-items-center text-light mb-2">
                            <i class="bi bi-people-fill me-2"></i>
                            <small>{{ squad.memberCount || 1 }} Members</small>
                        </div>
                        <div class="d-flex align-items-center text-light mb-2">
                            <i class="bi bi-calendar3 me-2"></i>
                            <small>{{ formatDate(squad.createdAt) }}</small>
                        </div>
                        <div class="d-flex align-items-center text-light">
                            <i class="bi bi-person-fill me-2"></i>
                            <small>{{ squad.usernameCreated === userStore.user.name ? 'Created by You' : `Created by User ${squad.usernameCreated}` }}</small>
                        </div>
                    </div>

                    <!-- Card Footer -->
                    <div class="border-top border-secondary pt-3 d-flex justify-content-between align-items-center">
                        <span class="text-neon small fw-semibold">
                            Join Room <i class="bi bi-arrow-right"></i>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Create Squad Modal -->
        <div class="modal fade" :class="{ show: showModal, 'd-block': showModal }" tabindex="-1" v-if="showModal">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header border-bottom border-secondary">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-shield-plus text-primary me-2" style="font-size: 1.5rem;"></i>
                            <h5 class="modal-title text-white mb-0">Create New Squad</h5>
                        </div>
                        <button type="button" class="btn-close btn-close-white" @click="showModal = false"></button>
                    </div>
                    
                    <div class="modal-body">
                        <!-- Squad Name Input -->
                        <div class="mb-4">
                            <label class="form-label text-white fw-semibold">Squad Name</label>
                            <input 
                                v-model="newSquadName" 
                                type="text" 
                                class="form-control" 
                                placeholder="Enter squad name..."
                            >
                        </div>

                        <!-- Friend Search -->
                        <div class="mb-4">
                            <label class="form-label text-white fw-semibold">Invite Friends (Max 3)</label>
                            <div class="input-group">
                                <input 
                                    v-model="friendName" 
                                    @keyup.enter="fetchFriends"
                                    type="text" 
                                    class="form-control" 
                                    placeholder="Search friends..."
                                >
                                <button @click="fetchFriends" class="btn btn-outline-primary" type="button">
                                    <i class="bi bi-search"></i>
                                </button>
                            </div>
                        </div>

                        <!-- Friends List -->
                        <div class="mb-3">
                            <div v-if="friends.length === 0 && !friendName" class="text-center text-white py-3">
                                <i class="bi bi-person-x me-2"></i>No friends found
                            </div>
                            <div v-else-if="friends.length > 0">
                                <h6 class="text-white mb-3">Available Friends</h6>
                                <div class="friend-list" style="max-height: 200px; overflow-y: auto;">
                                    <div 
                                        v-for="friend in friends" 
                                        :key="friend.userId"
                                        :class="['friend-item', 'rounded-2', 'p-3', 'mb-2', { 'selected': selectedFriends.includes(friend.userId) }]"
                                        @click="toggleFriendSelection(friend.userId)"
                                    >
                                        <div class="d-flex align-items-center">
                                            <div class="position-relative me-3">
                                                <div class="rounded-circle bg-primary d-flex align-items-center justify-content-center text-white fw-bold" style="width: 40px; height: 40px;">
                                                    {{ friend.username[0].toUpperCase() }}
                                                </div>
                                            </div>
                                            <div class="flex-grow-1">
                                                <div class="text-white fw-semibold">{{ friend.username }}</div>
                                                <small class="text-muted text-capitalize">{{ friend.status }}</small>
                                            </div>
                                            <div v-if="selectedFriends.includes(friend.userId)" class="text-primary">
                                                <i class="bi bi-check-circle-fill"></i>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                
                                <!-- Selected Count -->
                                <div v-if="selectedFriends.length > 0" class="text-primary small mt-2">
                                    <i class="bi bi-people-fill me-1"></i>
                                    {{ selectedFriends.length }}/3 friends selected
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary" @click="showModal = false">Cancel</button>
                        <button 
                            type="button" 
                            class="btn gaming-btn text-white" 
                            @click="submitSquad"
                            :disabled="!newSquadName.trim()"
                        >
                            <i class="bi bi-plus-lg me-2" @click="submitSquad"></i>Create Squad
                        </button>
                    </div>
                </div>
            </div>
        </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import apiGroup from "@/api/index";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router";

const router = useRouter();

const goToSquad = (squadId) => {
  router.push({
    path: "/plan/together",
    query: { squadId },
  });
};

const userStore = useUserStore();
const squads = ref([]);
const loading = ref(true);

const showModal = ref(false);
const newSquadName = ref("");
const friendName = ref("");
const selectedFriends = ref([]);
const friends = ref([]);

const fetchSquads = async () => {
  try {
    const res = await apiGroup.api({
      url: "/api/user/squads",
      method: "GET",
    });
    console.log(res.data)
    squads.value = res.data;
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const fetchFriends = async () => {
  if (friendName.value.length === 0) {
    alert("입력은 해야지");
    return;
  }

  try {
    const res = await apiGroup.api({
      url: `/api/user/friend/${friendName.value}`,
      method: "GET",
    });
    friends.value = res.data.filter(friend => friend.userId !== userStore.user.userId);
    friendName.value = ""
  } catch (err) {
    console.error("친구 목록 불러오기 실패:", err);
  }
};

const toggleFriendSelection = (friendId) => {
                    const index = selectedFriends.value.indexOf(friendId);
                    if (index > -1) {
                        selectedFriends.value.splice(index, 1);
                    } else if (selectedFriends.value.length < 3) {
                        selectedFriends.value.push(friendId);
                    } else {
                        alert("최대 3명까지만 초대할 수 있습니다!");
                    }
                };

const submitSquad = async () => {
  try {
    const body = {
      squadName: newSquadName.value,
      invitedUserIds: selectedFriends.value,
    };
    const res = await apiGroup.api({
      url: "/api/user/squads",
      method: "POST",
      data: body,
    });
    squads.value.push(res.data);
    showModal.value = false;
    newSquadName.value = "";
    selectedFriends.value = [];
    fetchSquads();
  } catch (err) {
    console.error("Squad 생성 실패:", err);
    alert("Squad 생성에 실패했습니다.");
  }
};

onMounted(() => {
  fetchSquads();
});

const formatDate = (dateArray) => {
                    const [year, month, day, hour, minute] = dateArray;
                    return `${year}.${month.toString().padStart(2, '0')}.${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
                };
</script>

<style scoped>
.gaming-card {
            background: rgba(30, 41, 59, 0.9);
            border: 1px solid rgba(59, 130, 246, 0.3);
            backdrop-filter: blur(10px);
            transition: all 0.3s ease;
        }
        
        .gaming-card:hover {
            transform: translateY(-5px);
            border-color: #3b82f6;
            box-shadow: 0 10px 30px rgba(59, 130, 246, 0.3);
        }
        
        .gaming-btn {
            background: linear-gradient(45deg, #3b82f6, #8b5cf6);
            border: none;
            box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
            transition: all 0.3s ease;
        }
        
        .gaming-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(59, 130, 246, 0.6);
            background: linear-gradient(45deg, #2563eb, #7c3aed);
        }
        
        .status-online { color: #10b981; }
        .status-away { color: #f59e0b; }
        .status-offline { color: #6b7280; }
        
        .modal-content {
            background: rgba(30, 41, 59, 0.95);
            border: 1px solid rgba(59, 130, 246, 0.5);
            backdrop-filter: blur(15px);
        }
        
        .form-control, .form-select {
            background: rgba(55, 65, 81, 0.8);
            border: 1px solid rgba(75, 85, 99, 0.6);
            color: white;
        }
        
        .form-control:focus, .form-select:focus {
            background: rgba(55, 65, 81, 0.9);
            border-color: #3b82f6;
            box-shadow: 0 0 0 0.2rem rgba(59, 130, 246, 0.25);
            color: white;
        }
        
        .form-control::placeholder {
            color: rgba(156, 163, 175, 0.8);
        }
        
        .friend-item {
            background: rgba(55, 65, 81, 0.6);
            border: 1px solid transparent;
            transition: all 0.2s ease;
            cursor: pointer;
        }
        
        .friend-item:hover {
            background: rgba(75, 85, 99, 0.8);
            border-color: rgba(59, 130, 246, 0.5);
        }
        
        .friend-item.selected {
            background: rgba(59, 130, 246, 0.2);
            border-color: #3b82f6;
        }
        
        .squad-header {
            background: linear-gradient(45deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1));
            border-bottom: 1px solid rgba(59, 130, 246, 0.3);
        }
        
        .loading-spinner {
            border: 3px solid rgba(59, 130, 246, 0.3);
            border-top: 3px solid #3b82f6;
            animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .crown-icon {
            color: #fbbf24;
            filter: drop-shadow(0 0 5px rgba(251, 191, 36, 0.5));
        }
        
        .text-neon {
            color: #60a5fa;
            text-shadow: 0 0 10px rgba(96, 165, 250, 0.5);
        }
        
        .btn-close-white {
            filter: invert(1) grayscale(100%) brightness(200%);
        }
</style>