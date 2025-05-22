<template>
  <div class="p-4">
    <h2 class="text-xl font-bold mb-4">내 Squad 목록</h2>

    <button @click="showModal = true" class="mt-4 bg-blue-500 text-white px-4 py-2 rounded">Squad 만들기</button>

    <!-- 모달 -->
    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
      <div class="bg-white p-6 rounded shadow w-96">
        <h3 class="text-lg font-bold mb-2">🛡 Squad 만들기</h3>

        <label class="block mb-2">
          <span class="text-sm">방 제목</span>
          <input v-model="newSquadName" class="border w-full px-2 py-1 rounded mt-1" />
        </label>

        <div class="mt-4">
          <p class="text-sm mb-1">친구 초대 (최대 3명)</p>
          <input v-model="friendName" class="border w-full px-2 py-1 rounded mt-1" @keyup.enter="fetchFriends()" />
          <div v-if="friends.length === 0">친구 목록 없음</div>
          <div v-else>
            <div v-for="friend in friends" :key="friend.userId" class="flex items-center mb-1">
              <input
                v-if="friend.username !== userStore.user.username"
                type="checkbox"
                :value="friend.userId"
                name="friend"
                v-model="selectedFriends"
                :disabled="selectedFriends.length >= 3 && !selectedFriends.includes(friend.user_id)"
              />
              <span v-if="friend.username !== userStore.user.username" class="ml-2">{{ friend.username }}</span>
            </div>
            {{ selectedFriends }}
          </div>
        </div>

        <div class="flex justify-end mt-4">
          <button @click="showModal = false" class="mr-2 px-3 py-1 border rounded">취소</button>
          <button @click="submitSquad" class="px-3 py-1 bg-blue-600 text-white rounded">만들기</button>
        </div>
      </div>
    </div>

    <ul v-if="!loading">
      <li
        v-for="squad in squads"
        :key="squad.squadId"
        class="mb-2 cursor-pointer text-blue-600 hover:underline"
        @click="goToSquad(squad.squadId)"
      >
        ✅ {{ squad.squadName }}
      </li>
    </ul>

    <div v-else>불러오는 중...</div>
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
    friends.value = res.data;
  } catch (err) {
    console.error("친구 목록 불러오기 실패:", err);
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
</script>

<style scoped>
/* 모달 배경 애니메이션 등 추가해도 좋음 */
</style>
