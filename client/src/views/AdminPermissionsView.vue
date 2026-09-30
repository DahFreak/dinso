<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { activeLocale, translate } from '../i18n'
import PageHeader from '../components/PageHeader.vue'
import Panel from '../components/Panel.vue'

const t = (source: string, values?: Record<string, string | number>): string =>
  translate(activeLocale.value, source, values)

const users = ref([
  { id: 'system-admin-user', name: 'Alex Lund', company: 'SvenskeBanken' },
  { id: 'regular-admin-user', name: 'Maja Eklund', company: 'PensionsBolaget' },
  { id: 'regular-user', name: 'Linn Åström', company: 'SvenskeBanken' },
  { id: 'multi-user', name: 'Norah Sjöberg', company: 'PensionsBolaget' },
])

const searchQuery = ref('')
const selectedUser = ref<string | null>(null)
const selectedCompany = ref('Nordljus Teknik AB')

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => 
    u.name.toLowerCase().includes(q) || 
    u.company.toLowerCase().includes(q) ||
    u.id.toLowerCase().includes(q)
  )
})

const companies = ['Nordljus Teknik AB', 'Horisont Konsult AB']

// Mock permissions for the selected company/user
const permissions = ref<Record<string, string>>({
  'Läsa information (logga in i företagsportalen)': 'READ',
  'Godkänna ärenden.': 'READ',
  'Lägga till medarbetare.': 'WRITE',
  'Ändra lön.': 'NOT ALLOWED',
  'Registrera tjänstledighet.': 'NOT ALLOWED',
  'Avsluta anställning.': 'NOT ALLOWED',
})

function selectUser(userId: string) {
  selectedUser.value = userId
  // Reset permissions based on user (mock)
  const user = users.value.find(u => u.id === userId)
  if (user?.name === 'Alex Lund') {
    permissions.value = {
      'Läsa information (logga in i företagsportalen)': 'READ',
      'Godkänna ärenden.': 'WRITE',
      'Lägga till medarbetare.': 'WRITE',
      'Ändra lön.': 'WRITE',
      'Registrera tjänstledighet.': 'WRITE',
      'Avsluta anställning.': 'WRITE',
    }
  } else if (user?.name === 'Maja Eklund') {
    permissions.value = {
      'Läsa information (logga in i företagsportalen)': 'READ',
      'Godkänna ärenden.': 'READ',
      'Lägga till medarbetare.': 'WRITE',
      'Ändra lön.': 'READ',
      'Registrera tjänstledighet.': 'READ',
      'Avsluta anställning.': 'READ',
    }
  } else {
    permissions.value = {
      'Läsa information (logga in i företagsportalen)': 'READ',
      'Godkänna ärenden.': 'NOT ALLOWED',
      'Lägga till medarbetare.': 'NOT ALLOWED',
      'Ändra lön.': 'NOT ALLOWED',
      'Registrera tjänstledighet.': 'NOT ALLOWED',
      'Avsluta anställning.': 'NOT ALLOWED',
    }
  }
}

function setPermission(action: string, level: string) {
  permissions.value[action] = level
}

const permissionActions = [
  'Läsa information (logga in i företagsportalen)',
  'Godkänna ärenden.',
  'Lägga till medarbetare.',
  'Ändra lön.',
  'Registrera tjänstledighet.',
  'Avsluta anställning.',
]

const levels = ['READ', 'WRITE', 'NOT ALLOWED']
</script>

<template>
  <section>
    <PageHeader :title="t('Behörigheter')" :description="t('Administrera behörigheter för företagsanvändare.')" />
    
    <Panel :title="t('Administrera behörigheter')">
      <!-- User List Section -->
      <div v-if="!selectedUser" class="user-list-section">
        <div class="search-filter">
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Sök användare eller företag..." 
            class="search-input"
          />
          <span class="search-count">{{ filteredUsers.length }} användare</span>
        </div>
        
        <div class="user-list">
          <button 
            v-for="user in filteredUsers" 
            :key="user.id"
            class="user-card" 
            @click="selectUser(user.id)"
          >
            <div class="user-info">
              <h3>{{ user.name }}</h3>
              <p>{{ user.company }}</p>
            </div>
            <span class="arrow">→</span>
          </button>
        </div>
      </div>
      
      <!-- User Permissions Section -->
      <div v-if="selectedUser" class="permissions-section">
        <button class="back-btn" @click="selectedUser = null">← Tillbaka till användarlista</button>
        
        <h3>{{ users.find(u => u.id === selectedUser)?.name }}</h3>
        
        <!-- Company Dropdown -->
        <div class="company-dropdown">
          <label for="company-select">Välj företag:</label>
          <select id="company-select" v-model="selectedCompany" class="company-select">
            <option v-for="company in companies" :key="company" :value="company">{{ company }}</option>
          </select>
        </div>
        
        <!-- Permissions List -->
        <div class="permissions-list">
          <div v-for="action in permissionActions" :key="action" class="permission-row">
            <div class="permission-label">{{ action }}</div>
            <div class="permission-toggles">
              <button 
                v-for="level in levels" 
                :key="level"
                :class="['toggle-btn', { active: permissions[action] === level }]"
                @click="setPermission(action, level)"
              >
                {{ level === 'READ' ? 'Läs' : level === 'WRITE' ? 'Skriv' : 'Ej tillåten' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Panel>
  </section>
</template>

<style scoped>
.user-list-section { padding: 0; }
.search-filter { display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem; }
.search-input { padding: 0.5rem 1rem; border: 1px solid var(--border); border-radius: var(--radius-control); font-size: 1rem; flex-grow: 1; background: var(--surface); color: var(--ink); }
.search-count { color: var(--muted); font-size: 0.9rem; }
.user-list { display: flex; flex-direction: column; gap: 0.5rem; }
.user-card { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 1rem; border: 1px solid var(--border); border-radius: var(--radius-panel); 
  background: var(--surface); text-align: left; cursor: pointer; 
  transition: background 0.2s; 
}
.user-card:hover { background: var(--hover); }
.user-info h3 { margin: 0; font-size: 1.1rem; }
.user-info p { margin: 0.2rem 0 0; color: var(--muted); font-size: 0.9rem; }
.arrow { font-size: 1.2rem; color: var(--muted); }
.back-btn { background: none; border: none; color: var(--primary); cursor: pointer; font-size: 1rem; padding: 0; margin-bottom: 1rem; }
.company-dropdown { margin: 1rem 0; }
.company-dropdown label { display: block; font-weight: 600; margin-bottom: 0.3rem; }
.company-select { padding: 0.5rem; font-size: 1rem; width: 100%; border: 1px solid var(--border); border-radius: var(--radius-control); background: var(--surface); color: var(--ink); }
.permissions-list { border-top: 1px solid var(--border); margin-top: 1rem; padding-top: 1rem; }
.permission-row { display: flex; justify-content: space-between; align-items: center; padding: 0.8rem 0; border-bottom: 1px solid var(--border-light); }
.permission-label { font-weight: 500; flex: 1; }
.permission-toggles { display: flex; gap: 0.3rem; }
.toggle-btn { padding: 0.3rem 0.7rem; border: 1px solid var(--border); border-radius: var(--radius-control); background: var(--surface); cursor: pointer; font-size: 0.8rem; color: #000000; min-width: 80px; text-align: center; }
.toggle-btn.active { background: var(--primary); color: #ffffff; border-color: var(--primary); min-width: 80px; text-align: center; }
</style>