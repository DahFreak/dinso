<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { activeLocale, translate } from '../i18n'
import PageHeader from '../components/PageHeader.vue'
import Panel from '../components/Panel.vue'
import { useDemoSessionStore } from '../stores/demoSession'

const t = (source: string, values?: Record<string, string | number>): string =>
  translate(activeLocale.value, source, values)

const session = useDemoSessionStore()

const apiUrl = import.meta.env.VITE_API_URL?.replace(/\/$/, '') ?? ''

const users = ref<Array<{ id: string; name: string; company: string }>>([])
const selectedUser = ref<string | null>(null)
const selectedCompany = ref<string | null>(null)
const companies = ref<string[]>([])
const permissions = ref<Record<string, string>>({})
const isLoading = ref(false)
const isSaving = ref(false)
const permissionLoaded = ref(false)
const allCompanyPermissions = ref<Record<string, Record<string, string>>>({})
const saveStatus = ref<{ message: string; type: 'success' | 'error' } | null>(null)

interface PermissionData {
  permissionType: string
  level: string
}

const permissionLabels: Record<string, string> = {
  LOGIN_COMPANY_PORTAL: 'Login',
  APPROVE_CASES: 'EditTicket',
  ADD_EMPLOYEES: 'AddEmployee',
  CHANGE_SALARY: 'EditSalary',
  REGISTER_LEAVE: 'EditVacation',
  TERMINATE_EMPLOYMENT: 'TerminateEmployment',
}

const permissionActionKeys = Object.keys(permissionLabels)
const levels = ['READ', 'WRITE', 'NOT_ALLOWED']

onMounted(async () => {
  if (session.activePortal === 'SYSTEM' && session.sessionToken) {
    try {
      const response = await fetch(`${apiUrl}/api/admin/users`, {
        headers: { Authorization: `Bearer ${session.sessionToken}` },
      })
      if (response.ok) {
        const userData = await response.json()
        users.value = userData.map((p: any) => ({
          id: p.id,
          name: p.name,
          company: '',
        }))
      } else {
        console.error('Failed to fetch users:', response.status)
      }
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }
})

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u => 
    u.name.toLowerCase().includes(q) || 
    u.company.toLowerCase().includes(q) ||
    u.id.toLowerCase().includes(q)
  )
})

const searchQuery = ref('')
const showSaveAllButton = ref(false)

async function selectUser(userId: string) {
  selectedUser.value = userId
  selectedCompany.value = null
  permissions.value = {}
  companies.value = []
  permissionLoaded.value = false
  showSaveAllButton.value = false
  saveStatus.value = null
  allCompanyPermissions.value = {}

  if (!session.sessionToken) return

  try {
    const response = await fetch(`${apiUrl}/api/admin/users/${userId}/permissions`, {
      headers: { Authorization: `Bearer ${session.sessionToken}` },
    })
    if (response.ok) {
      const data = await response.json()
      const companyPermissions = data.companies
      
      allCompanyPermissions.value = companyPermissions
      
      const companyNames = Object.keys(companyPermissions)
      companies.value = companyNames
      selectedCompany.value = companyNames[0] || null
      
      if (selectedCompany.value) {
        const permMap = companyPermissions[selectedCompany.value]
        permissionLoaded.value = true
        for (const action of permissionActionKeys) {
          permissions.value[action] = permMap[action] || 'NOT_ALLOWED'
        }
      }
    } else {
      console.error('Failed to fetch permissions:', response.status)
    }
  } catch (error) {
    console.error('Error fetching permissions:', error)
  }
}

watch(selectedCompany, (newCompany) => {
  if (!newCompany || !selectedUser.value) return
  const permMap = allCompanyPermissions.value[newCompany]
  if (!permMap) return
  permissions.value = {}
  for (const action of permissionActionKeys) {
    permissions.value[action] = permMap[action] || 'NOT_ALLOWED'
  }
  showSaveAllButton.value = false
})

function setPermission(action: string, level: string) {
  permissions.value[action] = level
  showSaveAllButton.value = true
}

async function savePermissions() {
  if (!selectedUser.value || !selectedCompany.value || !session.sessionToken) return

  isSaving.value = true
  saveStatus.value = null
  try {
    const payload: Record<string, Record<string, string>> = {}
    payload[selectedCompany.value] = {}
    for (const action of permissionActionKeys) {
      payload[selectedCompany.value][action] = permissions.value[action]
    }

    const response = await fetch(`${apiUrl}/api/admin/users/${selectedUser.value}/permissions`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${session.sessionToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (response.ok) {
      allCompanyPermissions.value[selectedCompany.value] = { ...permissions.value }
      saveStatus.value = { message: 'Behörigheter sparade!', type: 'success' }
      setTimeout(() => {
        if (saveStatus.value?.type === 'success') {
          saveStatus.value = null
        }
      }, 3000)
    } else {
      console.error('Failed to save permissions:', response.status)
      saveStatus.value = { message: 'Kunde inte spara behörigheter.', type: 'error' }
    }
  } catch (error) {
    console.error('Error saving permissions:', error)
    saveStatus.value = { message: 'Ett fel inträffade vid sparande.', type: 'error' }
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <section>
    <PageHeader :title="t('Behörigheter')" :description="t('Administrera behörigheter för företagsanvändare.')" />
    
    <Panel :title="t('Administrera behörigheter')">
      <div v-if="isLoading && !users.length" class="loading">Laddar användare...</div>
      
      <!-- User List Section -->
      <div v-if="!selectedUser && !isLoading" class="user-list-section">
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
      <div v-if="selectedUser && !isLoading" class="permissions-section">
        <button class="back-btn" @click="selectedUser = null">← Tillbaka till användarlista</button>
        
        <div class="user-header">
          <h3>{{ users.find(u => u.id === selectedUser)?.name }}</h3>
          <span class="user-id">{{ selectedUser }}</span>
        </div>
        
        <div v-if="permissionLoaded" class="permissions-container">
          <!-- Company Dropdown -->
          <div class="company-dropdown">
            <label for="company-select">Välj företag:</label>
            <select id="company-select" v-model="selectedCompany" class="company-select">
              <option v-for="company in companies" :key="company" :value="company">{{ company }}</option>
            </select>
          </div>
          
          <!-- Permissions List -->
          <div class="permissions-list">
            <div v-for="action of permissionActionKeys" :key="action" class="permission-row">
              <div class="permission-label">{{ permissionLabels[action] || action }}</div>
              <div class="permission-toggles">
                <button 
                  v-for="level in levels" 
                  :key="level"
                  :class="['toggle-btn', { active: permissions[action] === level }]"
                  @click="setPermission(action, level)"
                >
                  {{ level === 'READ' ? 'Läs' : level === 'WRITE' ? 'Skriv' : level === 'NOT_ALLOWED' ? 'Ej tillåten' : level }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="loading">Laddar behörigheter...</div>
        
        <!-- Save Bar (always visible when permissions are loaded) -->
        <div v-if="selectedUser && permissionLoaded" class="save-bar">
          <button 
            class="save-all-btn" 
            @click="savePermissions"
            :disabled="isSaving"
          >
            {{ isSaving ? 'Sparar...' : 'Spara ändringar' }}
          </button>
          <span v-if="saveStatus" class="save-status" :class="[saveStatus.type]">
            {{ saveStatus.message }}
          </span>
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

/* Save bar at bottom */
.save-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}
.save-all-btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: var(--radius-control);
  background: var(--primary);
  color: #ffffff;
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: var(--button-tracking, 0);
  text-transform: var(--button-case, none);
  transition: background-color 0.16s ease, border-color 0.16s ease, transform 0.16s ease;
  min-width: 80px;
  text-align: center;
}
.save-all-btn:hover:not(:disabled) {
  background: var(--primary-strong);
  border-color: var(--primary-strong);
}
.save-all-btn:active:not(:disabled) {
  transform: translateY(1px);
}
.save-all-btn:disabled {
  background: var(--primary);
  color: var(--primary-contrast);
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
  min-width: 80px;
  text-align: center;
}
.save-status {
  font-size: 0.9rem;
  font-weight: 500;
}
.save-status.success { color: var(--success); }
.save-status.error { color: var(--error); }
</style>
  border: none;
  border-radius: var(--radius-control);
  background: var(--primary);
  color: var(--primary-contrast);
  font-weight: 700;
  font-size: 0.95rem;
  letter-spacing: var(--button-tracking, 0);
  text-transform: var(--button-case, none);
  transition: background-color 0.16s ease, border-color 0.16s ease, transform 0.16s ease;
  min-width: 80px;
  text-align: center;
}
.save-all-btn:hover:not(:disabled) {
  background: var(--primary-strong);
  border-color: var(--primary-strong);
}
.save-all-btn:active:not(:disabled) {
  transform: translateY(1px);
}
.save-all-btn:disabled {
  background: var(--primary);
  color: var(--primary-contrast);
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
  min-width: 80px;
  text-align: center;
}